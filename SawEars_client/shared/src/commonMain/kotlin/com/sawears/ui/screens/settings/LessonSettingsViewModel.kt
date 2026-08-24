package com.sawears.ui.screens.lessonsettings


import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.LocalSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

/**
 * View model for lesson settings screen
 * This viewmodel inherits from settings view model as it offers changes to both global,
 * persistent settings and local lesson settings
 *
 * @property lessonId id representing the lesson to allow settings to be mapped to lesson
 * @param lessonSettingsRepository local settings repository storing settings for specific lessons
 * @param settingsRepository repository allowing certain global changes to be persisted
 */
class LessonSettingsViewModel(
    val lessonId: Int,
    private val lessonSettingsRepository: LocalSettingsRepository,
    private val settingsRepository: SettingsRepository
): SettingsViewModel(settingsRepository) {

    val localSettings: StateFlow<LessonSettings> = lessonSettingsRepository
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
           localSettings.value.copy(
               tempo = tempoBpm
           )
       )
    }

    /**
     * Update range min, locally and globally
     *
     * @param pitchMidi
     */
    override fun updateRangeMin(pitchMidi: Int){
        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            localSettings.value.copy(
                rangeMin = pitchMidi
            )
        )

        // update setting globally
        super.updateRangeMin(pitchMidi)
    }

    /**
     * Update range max, locally and globally
     *
     * @param pitchMidi
     */
    override fun updateRangeMax(pitchMidi: Int){
        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            localSettings.value.copy(
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

        require(difficulty in 1..5){
            "Difficulty must be an integer between 1 and 5"
        }
        // update setting locally
        lessonSettingsRepository.set(
            lessonId,
            localSettings.value.copy(
                difficulty = difficulty
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
            localSettings.value.copy(
                mode = mode
            )
        )

        // update setting globally
        super.updateMode(mode)
    }

}

/**
 * Class to hold local lesson settings
 */
@Serializable
data class LessonSettings(
    val tempo: Int,
    val difficulty: Int,
    val mode: LessonMode,
    val rangeMin: Int,
    val rangeMax: Int
)

@Serializable
enum class LessonMode {
    PRACTICE,
    PERFORMANCE
}