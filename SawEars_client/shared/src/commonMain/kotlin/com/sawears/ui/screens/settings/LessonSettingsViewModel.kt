package com.sawears.ui.screens.lessonsettings


import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.domain.songModel.Song
import com.sawears.domain.songModel.TranspositionException
import com.sawears.domain.settings.Difficulty
import com.sawears.ui.screens.settings.SettingsError
import com.sawears.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View model for lesson settings screen
 *
 * Implementation notes:
 * - [LessonSettingsViewModel] owns transposition logic, ensuring songs are always transposed to the
 *  centre of an instrument's range (where possible)
 * - Intended behaviour is that controlling settings for a given lesson
 * also changes it globally via the settings repository, with the exception of settings related
 * to a specific song (e.g. tempo). This class is also where auto transposition is triggered in
 * response to range changes.
 * - Lesson must already have been registered in the settings repository before initializing
 * view model, otherwise an error will be thrown
 *
 * @property lessonId id representing the lesson to allow settings to be mapped to lesson
 * @param lessonSettingsRepository a [LessonSettingsRepository] storing lesson specific settings
 * @param settingsRepository a [SettingsRepository] allowing application level settings to be persisted
 * @param autoTranspose flag controlling whether changing rangeMin / rangeMax trigger transposition
 */
class LessonSettingsViewModel(
    val lessonId: Int,
    private val lessonSettingsRepository: LessonSettingsRepository,
    private val settingsRepository: SettingsRepository,
    private val autoTranspose: Boolean = true
): SettingsViewModel(settingsRepository) {

    val lessonSettings: StateFlow<LessonSettings> = lessonSettingsRepository
        .settingsFor(lessonId)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = lessonSettingsRepository.get(lessonId)
        ?: error("Unable to find settings for lesson $lessonId")
    )


    private data class RangeUpdate(
        val minMidi: Int,
        val maxMidi: Int
    )

    /*
     Channel to ensure that range updates are made sequentially,
     avoiding a potential race condition if range update methods
     are called
     */
    private val rangeUpdates =
        Channel<RangeUpdate>(capacity = Channel.UNLIMITED)

    // tracks whether instrument range is wide enough to transpose song into
    val _transpositionStatus = MutableStateFlow(
        inRange(
            currentRepositorySettings().rangeMin,
            currentRepositorySettings().rangeMax,
            currentRepositorySettings().song
        )
    )
    val transpositionStatus = _transpositionStatus.asStateFlow()

    /*
    Launch a coroutine to track range updates, ensuring they are sent to the repository
    sequentially in the order the requests are made.
    */
    init {
        viewModelScope.launch {
            for (range in rangeUpdates) {
                try {
                    settingsRepository.setRange(
                        range.minMidi,
                        range.maxMidi
                    )
                    clearError()
                } catch (e: Exception){
                    setError(SettingsError.FailedToPersist)
                }
            }
        }
    }

    /*
    Handle initial autoTransposition when viewModel is first initialized.
    This ensures the song will be correctly transposed if the user does not
    adjust the intrument range
     */
    init {
        if (autoTranspose){
            val settings = currentRepositorySettings()

            val transposedSong = try {
                settings.song.adaptForRange(
                    settings.rangeMin,
                    settings.rangeMax
                ).also {
                    _transpositionStatus.value = true
                }
            } catch (e: TranspositionException){
                settings.song.also {
                    _transpositionStatus.value = false
                }
            }

            lessonSettingsRepository.set(
                lessonId,
                lessonSettings.value.copy(
                    song = transposedSong
                )
            )
        }
    }


    /**
     * Update tempo, scoped to the lesson locally
     *
     * @param tempoBpm
     */
    fun updateTempo(tempoBpm: Int){
        // tempo cannot be 0 or negative
        if (tempoBpm < 1) {
            setError(SettingsError.InvalidTempo)
            return
        }

        clearError()

        // update setting (local only)
       lessonSettingsRepository.set(
           lessonId,
           lessonSettings.value.copy(
               tempo = tempoBpm
           )
       )
    }

    /**
     * Update instrument range, locally and globally.
     *
     * Implementation note: when updating instrument range (max or min)
     * from the lesson settings screen, the song will automatically be
     * transposed to centre the melody on the instrument's playable range.
     *
     * This setting can be disabled by changing the autoTranspose flag at the view model level,
     * but for initial release, auto transposition is intended default behaviour.
     */
    override fun updateRange(rangeMinMidi: Int, rangeMaxMidi: Int){
        if (!validateRange(rangeMinMidi, rangeMaxMidi)) return

        val settings = currentRepositorySettings()

        // this is where auto transposition is applied, if autoTranspose flag set to tue
        val transposedSong = try {
            if (autoTranspose) {
                settings.song
                    .adaptForRange(rangeMinMidi, rangeMaxMidi)
                    .also {
                        _transpositionStatus.value = true
                    }
            } else {
                settings.song.also{
                    _transpositionStatus.value = inRange(rangeMinMidi,rangeMaxMidi,it)
                }
            }
        } catch (e: TranspositionException) {
            // allow small ranges to be set without triggering error, but record state
            // to show warning in UI
            settings.song.also {
                _transpositionStatus.value = false
            }
        }

        // set new values at lesson level
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                song = transposedSong,
                rangeMax = rangeMaxMidi,
                rangeMin = rangeMinMidi
            )
        )
        clearError()
        // send updates to channel to update app level repository
        rangeUpdates.trySend(
            RangeUpdate(
                rangeMinMidi,
                rangeMaxMidi
            )
        )

    }

    /*
    Validates whether instrument range >= song range
     */

    /**
     * Update range min, locally and globally
     *
     * If [autoTranspose] flag is true, updating this setting
     * will trigger song transposition.
     *
     * @param rangeMinMidi
     */
    override fun updateRangeMin(rangeMinMidi: Int){
        val settings = currentRepositorySettings()
        updateRange(rangeMinMidi, settings.rangeMax)

    }

    /**
     * Update range max, locally and globally
     *
     * If [autoTranspose] flag is true, updating this setting
     * will trigger song transposition.
     *
     * @param rangeMaxMidi
     */
    override fun updateRangeMax(rangeMaxMidi: Int){
        val settings = currentRepositorySettings()
        updateRange(settings.rangeMin, rangeMaxMidi)
    }

    private fun inRange(rangeMinMidi: Int, rangeMaxMidi: Int, song: Song): Boolean {
        return  (rangeMaxMidi - rangeMinMidi) >=
                (song.songMaxMidi - song.songMinMidi)
    }


    /**
     * Update difficulty, locally and globally
     *
     * @param difficulty
     */
    override fun updateDifficulty(difficulty: Int){

        require(difficulty in 1..enumValues<Difficulty>().size){
            "Int value outside range of possible difficulty enums"
        }

        val difficultyEnum = Difficulty.entries[difficulty-1]

        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                difficulty = difficultyEnum
            )
        )
        // update setting globally
        super.updateDifficulty(difficulty)
    }

    /**
     * Update mode, locally and globally
     */
    override fun updateMode(mode: LessonMode){

        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                mode = mode
            )
        )

        // update setting globally
        super.updateMode(mode)
    }

    override fun updatePitchMargin(marginMidi: Int) {
        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                pitchDisplayMarginMidi = marginMidi
            )
        )
        // persist lesson in settings repository
        super.updatePitchMargin(marginMidi)
    }

    private fun currentRepositorySettings(): LessonSettings =
        requireNotNull(lessonSettingsRepository.get(lessonId)){
            "Unable to find settings for lesson $lessonId"
        }


}

/**
 * Class to hold local lesson settings
 *
 * @property song
 * @property tempo
 * @property difficulty
 * @property mode
 * @property rangeMin
 * @property rangeMax
 * @property pitchDisplayMarginMidi
 */
data class LessonSettings(
    val song: Song,
    val tempo: Int,
    val difficulty: Difficulty,
    val mode: LessonMode,
    val rangeMin: Int,
    val rangeMax: Int,
    val pitchDisplayMarginMidi: Int
)


enum class LessonMode {
    PRACTICE,
    PERFORMANCE
}

