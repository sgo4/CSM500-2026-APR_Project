package com.sawears.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.sawears.AppContainer
import com.sawears.ui.lesson.components.PitchCurveInput
import kotlinx.coroutines.launch
import com.sawears.model.Song
import com.sawears.model.pitch.PitchResult
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
 */
class LessonViewModel(
    container: AppContainer
): ViewModel() {

    val playbackClock = container.playbackClock

    val pitchCurveInput = PitchCurveInput()
    val loadSong = container.loadSong
    val audioPlayer = container.audioPlayer
    private var playbackJob: Job? = null
    private var pitchCaptureJob: Job? = null
    val estimator = container.estimator
    var lessonPlayer: LessonPlayer? = null
    val audioInputSource = container.audioInputSource
    private val _state = MutableStateFlow(LessonUiState())
    val lessonState = _state.asStateFlow()
    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set



    fun startPitchPrediction(){
        println("starting pitch pred")
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

    fun stopPitchPrediction(){
        _state.update {
            it.copy(
                recordAudio = false
            )
        }
    }

    fun loadSong(title: String) {

        viewModelScope.launch {
            loading = true
            error = null

            try {
                val song = loadSong.load("files/midijson/$title.json")!!
                lessonPlayer = LessonPlayer(song)
                setPlayBackSong(song)
                _state.update {
                    it.copy(
                        song= song,
                        player = lessonPlayer
                    )
                }
            } catch (exception: Exception) {
                error = exception.message ?: "Failed to load data"
            } finally {
                loading = false
            }

        }
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

    fun setPlayBackSong(song: Song){
        audioPlayer.audioSource.loadInput(song)
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
        audioPlayer.close() // close and drain audio line
        audioInputSource.close() // close audioinput source
        pitchCurveInput.close()
        super.onCleared()
    }
}
data class DisplayNote(
    val pitchMidi: Float,
    val durationRelative: Float,
    val startsOnBeat: Float,
    val isCurrentNote: Boolean
)

data class LessonUiState(
    val song: Song? = null,
    val player: LessonPlayer? = null,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val songTitle: String? = null,
    val recordAudio: Boolean = false,
    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
)

/**
 * ViewModel representation of a song, keeping track of playback head, elapses time
 */
class LessonPlayer(
    val song: Song,
    defaultTempo: Int = 90,
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
    var tempo = song.tempo?: defaultTempo
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
     * @param currentTime
     */
    private fun updatePlayBackHead(currentTimeMillis: Long){
        if (systemTimeMillis == null){
            systemTimeMillis = currentTimeMillis
        }
        val elapsedTimeMs = currentTimeMillis - systemTimeMillis!!
        val elapsedTimeBeats = elapsedTimeMs / (60_000f / tempo)
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


