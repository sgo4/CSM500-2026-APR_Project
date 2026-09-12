package com.sawears.ui.screens.lessonsettings


import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.Song
import com.sawears.model.TranspositionException
import com.sawears.model.pitch.settings.Difficulty
import com.sawears.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * View model for lesson settings screen
 * This viewmodel inherits from settings view model as it offers changes to both global,
 * persistent settings and local lesson settings
 *
 * @property lessonId id representing the lesson to allow settings to be mapped to lesson
 * @param lessonSettingsRepository local settings repository storing settings for specific lessons
 * @param settingsRepository repository allowing certain global changes to be persisted
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
     * Update range min, locally and globally
     *
     * Implementation note: when updating rangeMin or rangeMax
     * from the lesson settings screen, the song will automatically be
     * transposed to centre the melody on the instrument's playable range.
     *
     * This setting can be disabled by changing the autoTranspose flag at the view model level,
     * but for initial release, auto transposition is intended default behaviour.
     *
     * @param pitchMidi
     */
    override fun updateRangeMin(pitchMidi: Int){

        // handle auto transposition if configured
        var song: Song = lessonSettings.value.song
        if (autoTranspose){
            try {
                song = song
                    .adaptForRange(
                        pitchMidi,
                        lessonSettings.value.rangeMax
                    )
            } catch (e: TranspositionException){
                error("$e. Range ${lessonSettings.value.rangeMin} - ${lessonSettings.value.rangeMax} is not large enough for selected song.")

            }
        }

        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                song = song,
                rangeMin = pitchMidi
            )
        )

        // update setting globally
        super.updateRangeMin(pitchMidi)

    }

    /**
     * Update range max, locally and globally
     *
     * Implementation note: when updating rangeMin or rangeMax
     * from the lesson settings screen, the song will automatically be
     * transposed to centre the melody on the instrument's playable range.
     *
     * This setting can be disabled by changing the autoTranspose flag at the view model level,
     * but for initial release, auto transposition is intended default behaviour.
     *
     * @param pitchMidi
     */
    override fun updateRangeMax(pitchMidi: Int){
        // handle auto transposition if configured
        var song: Song = lessonSettings.value.song
        if (autoTranspose){
            try {
                song = song
                    .adaptForRange(
                        lessonSettings.value.rangeMin,
                        pitchMidi
                    )
            } catch (e: TranspositionException){
                error("$e. Range ${lessonSettings.value.rangeMin} - ${lessonSettings.value.rangeMax} is not large enough for selected song.")

            }
        }

        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            lessonSettings.value.copy(
                song = song,
                rangeMax = pitchMidi
            )
        )
        // update setting globally
        super.updateRangeMax(pitchMidi)
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
        // update setting globally
        super.updatePitchMargin(marginMidi)
    }

}

/**
 * Class to hold local lesson settings
 * //TODO Kdoc
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