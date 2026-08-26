package com.sawears.ui.screens.lesson


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.sawears.AppContainer
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.ui.screens.lesson.components.PitchCurveInput
import kotlinx.coroutines.launch
import com.sawears.model.Song
import com.sawears.ui.screens.lessonsettings.LessonSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive


/**
 * Experimental working class for a view model holding lesson state
 *
 * Song
 * Time
 * Tempo
 * Current pitch
 * // TODO finish KDOC
 */
class LessonViewModel(
    container: AppContainer, // TODO just pass in the actual classes needed
    val lessonId: Int,
    val lessonSettingsRepository: LessonSettingsRepository
): ViewModel() {

    init {
        println("Initializing lesson viewmodel") // TODO delete debug text
    }

    // TODO observe lesson settings from central repository
    val st = lessonSettingsRepository.settingsFor(lessonId)

    // state
    private val _state = MutableStateFlow(LessonUiState())
    val lessonState = _state.asStateFlow()

    private val _settings = MutableStateFlow(lessonSettings)
    val settings = _settings.asStateFlow()

    // TODO is this necessary / the best way of handling things?
    var error by mutableStateOf<String?>(null)
        private set

    // player class to help with managing animations etc.
    var lessonPlayer: LessonPlayer = LessonPlayer(lessonSettings.song, lessonSettings.tempo)
    // TODO figure out if you want to use a playback clock
    val playbackClock = container.playbackClock

    // audio in
    private var pitchCaptureJob: Job? = null

    private val audioInputSourceFactory = container.audioInputSourceFactory
    private var _audioInputSource: AudioInputSource? = null

    // audio out
    val audioPlayer = container.audioPlayer
    private var playbackJob: Job? = null

    // pitch tracking
    val estimator = container.estimator
    val pitchCurveInput = PitchCurveInput() // buffer to hold pitch estimates for pitch graph

    init {
        // preload audioplayer with song ready for playback
        audioPlayer.audioSource.loadInput(settings.value.song)
    }
    /**
     * Open an audio channel and start generating pitch estimates
     */
    fun startPitchPrediction(){

        val audioInputSource = getAudioInputSource()

        pitchCaptureJob?.cancel()
        _state.update {
            it.copy(
                recordAudio = true
            )
        }

        pitchCaptureJob = viewModelScope.launch(Dispatchers.IO){
            while (isActive){
                val frame = audioInputSource.readFrame() ?: break
                val pitchResult = estimator.estimate(frame)
                pitchResult.frequencyHz?.let{
                    if (it > 0){
                        pitchCurveInput.submitPitch(pitchResult)
                    }
                }
            }
        }
    }

    /*
     * Get or create an audio input source
     */
    private fun getAudioInputSource(): AudioInputSource{
        _audioInputSource?.let { return it }

        val audioInputSource = audioInputSourceFactory.create()

        _audioInputSource = audioInputSource

        return audioInputSource
    }

    /**
     * Close audio channel and halt pitch prediction
     */
    fun stopPitchPrediction(){
        _state.update {
            it.copy(
                recordAudio = false
            )
        }
        pitchCaptureJob?.cancel()
    }



    fun toggleAudioPause(){

        _state.update {
            it.copy(
                audioPaused = !it.audioPaused
            )
        }
        if (_state.value.audioPaused){
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }

    }

    fun playAudio(){
        toggleAudioPause()
        if (_state.value.audioPaused) {
            playbackJob?.cancel()
            playbackJob = null
            return
        }


        playbackJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && !_state.value.audioPaused) {
                val windowLength = audioPlayer.writeWindow()

                if (windowLength < 1) {
                    audioPlayer.audioSource.reset()
                    break
                }
            }
        }

    }

    fun toggleLessonPlay(){
        _state.update {
            it.copy(
                paused = !it.paused
            )
        }
    }

    override fun onCleared(){
        audioPlayer.close() // close audio line
        _audioInputSource?.close() // close audioinput source
        pitchCurveInput.close()
        stopPitchPrediction()
    }
}


/**
 * UI layer representation of a single note including information about its
 * position in the melody
 */
data class DisplayNote(
    val pitchMidi: Float,
    val durationRelative: Float,
    val startsOnBeat: Float,
    val isCurrentNote: Boolean
)



/**
 * Player class that controlls the main animations
 */
class LessonPlayer(
    val song: Song,
    val tempoBpm: Int,
    val canvasWidthBeats: Float = 5f,
    val playBackHeadPosition: Float = 0.5f
){
    var playBackHeadBeats: Float = 0f
    var systemTimeMillis: Long? = null
    var noteTransitions = FloatArray(song.notes.size)
    var currentNoteIdx = 0
    var currentWindowStartBeats = 0f
    var currentWindowEndBeats = currentWindowStartBeats + canvasWidthBeats
    var latestConsumedNoteIdx = -1
    var consumedNotesBeats = 0f
    val pastBeats = canvasWidthBeats * playBackHeadPosition
    val futureBeats = canvasWidthBeats * (1-playBackHeadPosition)


    init {
        // populate noteTransitions with the end of each note
        var boundary = 0f
        for (i in noteTransitions.indices){
            boundary += song.notes[i].durationRelative.toFloat()
            noteTransitions[i] = boundary
        }
    }

    /*
     * Update the position of the playback head in beats elapsed based on time elapses
     *
     * @param currentTimeMillis
     */
    private fun updatePlayBackHead(currentTimeMillis: Long){
        if (systemTimeMillis == null){
            systemTimeMillis = currentTimeMillis
        }
        val elapsedTimeMs = currentTimeMillis - systemTimeMillis!!
        val elapsedTimeBeats = elapsedTimeMs / (60_000f / tempoBpm)
        playBackHeadBeats += elapsedTimeBeats
        systemTimeMillis = currentTimeMillis
    }

    /**
     * Calculate which notes are in view (fully or partially) at current time
     *
     * @param currentTimeMillis
     */
    fun notesInView(currentTimeMillis: Long): List<DisplayNote>{

        updatePlayBackHead(currentTimeMillis) // move playback head appropriate distance for time elapsed

        while (currentNoteIdx < noteTransitions.size && playBackHeadBeats >= noteTransitions[currentNoteIdx]){
            currentNoteIdx ++ // update current note idx
        }
        // calculate window start / end in terms of cummulative beats elapsed
        currentWindowStartBeats = playBackHeadBeats - pastBeats
        currentWindowEndBeats = playBackHeadBeats + futureBeats

        var windowStartIdx = latestConsumedNoteIdx
        var cummulativeBeats = maxOf(consumedNotesBeats,0f)
        while (windowStartIdx < song.notes.size-1 && cummulativeBeats <= currentWindowStartBeats){
            windowStartIdx++
            cummulativeBeats += song.notes[windowStartIdx].durationRelative.toFloat()
        }
        windowStartIdx = maxOf(windowStartIdx,0)
        latestConsumedNoteIdx = windowStartIdx - 1
        consumedNotesBeats = maxOf(cummulativeBeats - song.notes[windowStartIdx].durationRelative.toFloat(), 0f)

        val res = mutableListOf<DisplayNote>()
        var noteIdx = windowStartIdx
        cummulativeBeats = consumedNotesBeats
        while (noteIdx < song.notes.size-1 && cummulativeBeats <= currentWindowEndBeats){
            val note = song.notes[noteIdx]
            val durationRelative = note.durationRelative.toFloat()
            res.add(
                DisplayNote(
                    note.pitchMidi.toFloat(),
                    durationRelative,
                    cummulativeBeats,
                    isCurrentNote = noteIdx == currentNoteIdx
                )
            )
            cummulativeBeats += durationRelative
            noteIdx++
        }
        return res.toList()

    }
}


