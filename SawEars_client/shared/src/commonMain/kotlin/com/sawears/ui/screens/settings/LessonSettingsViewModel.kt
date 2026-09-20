package com.sawears.ui.screens.lessonsettings


import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.Song
import com.sawears.model.TranspositionException
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.settings.SettingsError
import com.sawears.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * View model for lesson settings screen
 *
 * This viewmodel inherits from settings view model as it offers changes to both global,
 * persistent settings and local lesson settings
 *
 * Implementation note: Intended behaviour is that controlling settings for a given lesson
 * also changes it globally via the settings repository, with the exception of settings related
 * to a specific song (e.g. tempo). This class is also where auto transposition is triggered in
 * response to range changes.
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

    // tracks whether instrument range is wide enough to transpose song into
    val _transpositionStatus = MutableStateFlow(
        inRange()
    )
    val transpositionStatus = _transpositionStatus.asStateFlow()

    /**
     * Update tempo, scoped to the lesson locally
     *
     * @param tempoBpm
     */
    fun updateTempo(tempoBpm: Int){

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

        var song = lessonSettings.value.song

        // this is where auto transposition is applied, if autoTranspose flag set to tue
        try {
            if (autoTranspose) {
                song = song.adaptForRange(rangeMinMidi, rangeMaxMidi)
                _transpositionStatus.value = true
            } else {
                _transpositionStatus.value = inRange()
            }
        } catch (e: TranspositionException) {
            // allow small ranges to be set without triggering error, but record state
            // to show warning in UI
            _transpositionStatus.value = false
        }

        viewModelScope.launch {
            try {
                settingsRepository.setRange(rangeMinMidi, rangeMaxMidi)

                lessonSettingsRepository.set(
                    lessonId,
                    lessonSettings.value.copy(
                        song = song,
                        rangeMax = rangeMaxMidi,
                        rangeMin = rangeMinMidi
                    )
                )
                clearError()
            } catch (e: Exception) {
                setError(SettingsError.FailedToPersist)
            }
        }
    }

    /*
    Validates whether instrument range >= song range
     */
    private fun inRange(): Boolean {
        return  (lessonSettings.value.rangeMax - lessonSettings.value.rangeMin) >=
                (lessonSettings.value.song.songMaxMidi - lessonSettings.value.song.songMinMidi)
    }
    /**
     * Update range min, locally and globally
     *
     * If [autoTranspose] flag is true, updating this setting
     * will trigger song transposition.
     *
     * @param rangeMinMidi
     */
    override fun updateRangeMin(rangeMinMidi: Int){
        val rangeMaxMidi = lessonSettings.value.rangeMax
        updateRange(rangeMinMidi, rangeMaxMidi)

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
        val rangeMinMidi = lessonSettings.value.rangeMin
        updateRange(rangeMinMidi, rangeMaxMidi)
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

