package com.sawears.ui.screens.lesson


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.sawears.AppContainer
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.ui.screens.lesson.components.PitchCurveInput
import kotlinx.coroutines.launch
import com.sawears.model.Song
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
    container: AppContainer // TODO just pass in the actual classes needed
): ViewModel() {

    // state
    private val _state = MutableStateFlow(LessonUiState())
    val lessonState = _state.asStateFlow()

    private val songDao = container.db.songDao()

    // TODO should this be more specific? More than one thing may need to load.
    var loading by mutableStateOf(false)
        private set

    // TODO is this necessary / the best way of handling things?
    var error by mutableStateOf<String?>(null)
        private set

    // player class to help with managing animations etc.
    var lessonPlayer: LessonPlayer? = null
        private set
    // TODO figure out if you want to use a playback clock
    val playbackClock = container.playbackClock

    private val loadSong = container.loadSong // repository for loading songs

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


    /**
     * TODO this is just to prove that the db is working. Logic should be improved.
     */
    suspend fun getResourcePath(title: String): String? {
        val matches = songDao.searchByTitleExact(title)
        if (matches.size == 1){
            return matches[0].resourcePath
        }
        return null
    }
    /**
     * Load a song based on provided resource path
     *
     * @param resourcePath (as retrieved from index database)
     */
    suspend fun loadSong(resourcePath: String) {

        loading = true
        error = null

        try {
            val song = loadSong.load(resourcePath)!! // load the resource
            lessonPlayer = LessonPlayer(song) // create lesson player for song
            audioPlayer.audioSource.loadInput(song) // load song into audio player
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


