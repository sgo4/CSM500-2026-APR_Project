package com.sawears.ui.screens.lesson


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.diagnostics.RealTimeLatencyStage
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.ui.screens.lesson.components.PitchCurveInput
import kotlinx.coroutines.launch
import com.sawears.domain.songModel.Song
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.pitch.PitchResult
import com.sawears.domain.settings.Difficulty
import com.sawears.domain.settings.DifficultySettingsProvider
import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlin.Float


/**
 * Experimental working class for a view model holding lesson state
 *
 * @property lessonId unique identifier mapping lesson to related entities such as settings
 * @property audioPlayerFactory creates an instance of platform specific audio player
 * @property estimator pitch estimator class
 * @property difficultySettingsProvider involved in translating difficulty levels to concrete settings
 * @property audioInputSourceFactory creates audio input source
 * @param lessonSettingsRepository
 * @property latencyTracker tracks processing latency for audio frames. Use a no op implementation for production.
 */
class LessonViewModel(
    val lessonId: Int,
    audioPlayerFactory: AudioPlayerFactory,
    val estimator: PitchEstimator,
    private val difficultySettingsProvider: DifficultySettingsProvider,
    private val audioInputSourceFactory: AudioInputSourceFactory,
    lessonSettingsRepository: LessonSettingsRepository,
    val latencyTracker: RealTimeLatencyTracker
): ViewModel() {

    val audioPlayer = audioPlayerFactory.create()

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
        initialSettings.mode
    ))
    private val _frameRenderState = MutableStateFlow(CanvasAnimationState(
        playBackHeadBeats=0f
    ))
    val lessonState = _state.asStateFlow()
    val frameRenderState = _frameRenderState.asStateFlow()

    // audio in
    private var pitchCaptureJob: Job? = null
    private var _audioInputSource: AudioInputSource? = null

    // audio out
    private var playbackJob: Job? = null

    // pitch tracking
    val pitchCurveInput = PitchCurveInput() // buffer to hold pitch estimates for pitch graph

    // animation timeline
    private var lessonPlayer = LessonPlayer(
        lessonState.value
    )

    init {
        // observe settings for changes that could affect the lesson (e.g. changes to tempo)
        observePlaybackSettings()
        // observe changes to lesson settings that affect state (e.g. mode, difficulty)
        observeLessonUiSettings()
        // provide initial animation state
        updateAnimationState(0L,-1f)
    }

    private fun observeLessonUiSettings(){
        lessonSettings
            .map{ settings ->
                LessonUiSettings(
                    pitchDisplayMarginMidi = settings.pitchDisplayMarginMidi,
                    difficulty = settings.difficulty,
                    mode = settings.mode
                )
            }
            .distinctUntilChanged()
            .onEach { settings ->
                _state.update {
                    it.copy(
                        pitchDisplayMarginMidi = settings.pitchDisplayMarginMidi.toFloat(),
                        mode = settings.mode,
                        leniencyThresholdCents = difficultySettingsProvider
                            .get(settings.difficulty)
                            .centsThreshold
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /*
     * Observe settings related to lesson / audio playback
     */
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
                _frameRenderState.update {
                    it.copy(
                        playBackHeadBeats = 0f,
                        notesInView=emptyList(),
                    )
                }

                accuracyTracker.reset()

                lessonPlayer = LessonPlayer(
                    lessonState.value
                )

                // provide initial animation state
                updateAnimationState(0L,-1f)


            }
            .launchIn(viewModelScope)
    }

    /**
     * Open an audio channel and start generating pitch estimates
     */
    fun startPitchPrediction(){

        // handle case where pitch capture is already active
        if (pitchCaptureJob?.isActive == true) return

        _state.update {
            it.copy(
                recordAudio = true
            )
        }

        // ensure blocking pitch operations happen on IO coroutine
        pitchCaptureJob = viewModelScope.launch(Dispatchers.IO){

            var audioInputSource: AudioInputSource? = null
            try {
                audioInputSource = getAudioInputSource()
                while (isActive) {

                    val frame = audioInputSource.readWindow() ?: break
                    latencyTracker.recordAtStage(frame.frameId, RealTimeLatencyStage.FRAME_READ)
                    latencyTracker.recordAtStage(frame.frameId, RealTimeLatencyStage.ESTIMATION_STARTED)
                    val pitchResult = estimator.estimate(frame).also{
                        latencyTracker.recordAtStage(frame.frameId, RealTimeLatencyStage.ESTIMATION_COMPLETE)
                    }

                    // submit pitch result without any checks - let receiving functions
                    // decide how to handle null frequencies etc.
                    pitchCurveInput.submitPitch(pitchResult)
                }
            } finally {
                audioInputSource?.close()
            }
        }
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
        pitchCaptureJob = null
        _audioInputSource?.close()
        _audioInputSource = null

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
     * Pause / unpause lesson
     *
     * Implementation note: As pitch detection is designed to be monophonic,
     * audio playback and lesson playback are designed to be mutually exclusive
     * to prevent audio output being registered as user input
     */
    fun toggleLessonPlay(){
        _state.update {
            it.copy(
                paused = !it.paused,
                audioPaused = true // guarantees audio and lesson cannot be unpaused at same time
            )
        }
    }

    /**
     * Pause / unpause audio playback
     *
     * Implementation note: As pitch detection is designed to be monophonic,
     * audio playback and lesson playback are designed to be mutually exclusive
     * to prevent audio output being registered as user input
     */
    fun toggleAudioPause(){

        _state.update {
            it.copy(
                paused = true, // guarantees lesson cannot be unpaused while audio is unpaused
                audioPaused = !it.audioPaused
            )
        }
        if (_state.value.audioPaused){
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
    }

    /**
     * Designed to map to a "play" button for audio playback. Handles state changes and
     * passes instructions to player if state is unpaused.
     */
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
     * Resets lesson back to beginning
     */
    fun restartLesson(){

        _state.update {
            it.copy(
                tempoBpm = lessonSettings.value.tempo,
                audioPaused = true
            )
        }
        _frameRenderState.update {
            it.copy(
                playBackHeadBeats = 0f,
                notesInView=emptyList(),
            )
        }

        accuracyTracker.reset()

        lessonPlayer = LessonPlayer(
            lessonState.value
        )

        // provide initial animation state
        updateAnimationState(0L,-1f)
    }

    /**
     * Function that calls when a new frame of animation is requested
     * Progresses animation timeline and exposes new values for drawable
     * components.
     *
     * @property frameDeltaNanos time delta between current and previous frame
     */
    fun onPlaybackFrame(
        frameDeltaNanos: Long,
        latestPitch: PitchResult?,
        paused: Boolean
    ){
        if (!paused) {
            updateAnimationState(frameDeltaNanos, latestPitch?.frequencyHz?.toFloat() ?: -1f)
        }
    }

    private fun updateAnimationState(frameDeltaNanos: Long, latestPitchHz: Float){

        val currentNote = lessonPlayer.currentNote()

        // determine whether user is "on pitch". If current note is null
        // user is considered on pitch by default (but the timeline will not
        // advance so no scoring update will occur)
        val isOnPitch = currentNote?.let {
            accuracyTracker.isOnPitch(
                it.toHz(),
                latestPitchHz,
                lessonState.value.leniencyThresholdCents
            )
        }?: true

        // get state for next frame - player handles most of the logic,
        // viewmodel simply provides values
        val animationSnapshot = lessonPlayer.nextFrame(
            frameDeltaMillis=frameDeltaNanos / 1_000_000f,
            mode=lessonState.value.mode,
            isOnPitch=isOnPitch,
        )
        // update accuracy tracker
        accuracyTracker.update(
            frameRenderState.value.notesInView,
            animationSnapshot.playBackHeadBeats,
            isOnPitch
        )
        // update state
        _frameRenderState.update {
            it.copy(
                notesInView = animationSnapshot.notesInView,
                noteAccuracies = accuracyTracker.noteAccuracies,
                playBackHeadBeats = animationSnapshot.playBackHeadBeats
            )
        }

    }

    /**
     * Safely clear view model and release resources
     */
    override fun onCleared(){
        // clear mic input resources
        stopPitchPrediction()
        // clear and release speaker out resources
        playbackJob?.cancel()
        audioPlayer.close() // close audio out line
        // close pitchCurveInput
        pitchCurveInput.close()
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

/*
 * Helper class holding other observable lesson ui settings, e.g. display margin,
 * mode, difficulty
 */
private data class LessonUiSettings(
    val pitchDisplayMarginMidi: Int,
    val difficulty: Difficulty,
    val mode: LessonMode
)
