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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive


/**
 * Experimental working class for a view model holding lesson state
 *
 * @param container
 */
class LessonViewModel(
    container: AppContainer, // TODO just pass in the actual classes needed
    val lessonId: Int,
    lessonSettingsRepository: LessonSettingsRepository
): ViewModel() {

    // settings

    /*
    Obtain latest settings for current lesson synchronously. This should work as it is not possible to start a lesson
     without first creating lesson settings in the lesson settings screen.
     */
    private val initialSettings = requireNotNull(lessonSettingsRepository.get(lessonId)){
        "unable to find settings for lesson with lesson Id $lessonId"
    }
    val lessonSettings = lessonSettingsRepository
        .settingsFor(lessonId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialSettings
        )

    private val accuracyTracker = AccuracyTracker()
    // state
    private val _state = MutableStateFlow(LessonUiState(
        initialSettings.song,
        initialSettings.tempo,
    ))
    val lessonState = _state.asStateFlow()



    // TODO is this necessary / the best way of handling things?
    var error by mutableStateOf<String?>(null)
        private set

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

    // animation timeline
    private var lessonPlayer = LessonPlayer(
        lessonState.value
    )

    init {
        // observe settings for changes that could affect the lesson (e.g. changes to tempo)
        observePlaybackSettings()
        // observe settings that affect display (e.g. zoom)
        observeDisplaySettings()
        // provide initial animation state
        updateAnimationState(0L)
    }


    private fun observeDisplaySettings(){
        lessonSettings
            .map{ it.pitchDisplayMarginMidi }
            .distinctUntilChanged()
            .onEach { marginMidi ->
                _state.update {
                    it.copy(
                        pitchDisplayMarginMidi = marginMidi.toFloat()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePlaybackSettings(){
        lessonSettings.map { settings ->
            PlaybackSettings(
                song = settings.song,
                tempo = settings.tempo
            )
        }
            .distinctUntilChanged()
            .onEach { settings ->

                // reset audio playback if song/tempo change
                playbackJob?.cancelAndJoin()
                audioPlayer.stop()
                audioPlayer.flush()
                audioPlayer.audioSource.loadInput(settings.song)

                // reset lesson Player on tempo change
                // currently this assumes the lesson restarts on playback
                // change and does not attempt to resume from the same position
                _state.update {
                    it.copy(
                        song = settings.song,
                        tempoBpm = settings.tempo,
                        audioPaused = true
                    )
                }

                lessonPlayer = LessonPlayer(
                    lessonState.value
                )
            }
            .launchIn(viewModelScope)
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

    /**
     * Function that calls when a new frame of animation is requested
     * Progresses animation timeline and exposes new values for drawable
     * components.
     *
     * @property frameDeltaNanos time delta between current and previous frame
     */
    fun onPlaybackFrame(frameDeltaNanos: Long){
        updateAnimationState(frameDeltaNanos)
    }

    /*
     * //TODO
     */
    private fun updateAnimationState(frameDeltaNanos: Long){
        // get state for next frame based on time delta
        val animationSnapshot = lessonPlayer.nextFrame(
            frameDeltaNanos / 1_000_000f
        )

        accuracyTracker.update(
            lessonState.value.notesInView,
            animationSnapshot.playBackHeadBeats,
            true // TODO actually get this information
        )
        // update state
        _state.update {
            it.copy(
                notesInView = animationSnapshot.notesInView,
                noteAccuracies = accuracyTracker.noteAccuracies,
                playBackHeadBeats = animationSnapshot.playBackHeadBeats
            )
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

/*
Helper class holding settings that directly affect playback and may require animations/
audio players etc. to be updated
 */
private data class PlaybackSettings(
    val song: Song,
    val tempo: Int
)
