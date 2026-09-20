package com.sawears.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel for managing settings
 *
 * Implementation Note: Open so it can be subclassed by other view models that access settings
 *
 * @param settingsRepository a [SettingsRepository] in which to store settings data
 */
open class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    /**
     * Expose settings to UI
     */
    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    protected val _error = MutableStateFlow<SettingsError?>(null)

    /**
     * Exposes error state
     */
    val error: StateFlow<SettingsError?> = _error.asStateFlow()

    open fun updateRangeMin(rangeMinMidi: Int){
        updateRange(
            rangeMinMidi=rangeMinMidi,
            rangeMaxMidi=settings.value.rangeMaxMidi
        )
    }

    open fun updateRangeMax(rangeMaxMidi: Int){
        updateRange(
            rangeMinMidi=settings.value.rangeMinMidi,
            rangeMaxMidi=rangeMaxMidi
        )
    }

    /*
    Handles common validation and persistence logic for range update functions
     */
    protected open fun updateRange(rangeMinMidi: Int, rangeMaxMidi: Int){

        if (!validateRange(rangeMinMidi,rangeMaxMidi)) return

        clearError()

        viewModelScope.launch {
            try {
                settingsRepository.setRange(rangeMinMidi, rangeMaxMidi)
            } catch (e: Exception){
                setError(SettingsError.FailedToPersist)
            }
        }

    }

    /*
    Common validation logic for setting instrument range
     */
    protected fun validateRange(
        rangeMinMidi: Int,
        rangeMaxMidi: Int
    ): Boolean {
        if (rangeMinMidi >= rangeMaxMidi){
            setError(SettingsError.RangeLessThanOne)
            return false
        }
        return true
    }

    open fun updateDifficulty(difficulty: Int){

        require(difficulty in 1..enumValues<Difficulty>().size){
            "Int value outside range of possible difficulty enums"
        }

        val difficultyEnum = Difficulty.from(difficulty)
        viewModelScope.launch {
            try {
                settingsRepository.setDifficulty(difficultyEnum)
            } catch (e: Exception){
                setError(SettingsError.FailedToPersist)
            }
        }

    }

    open fun updateMode(mode: LessonMode){
        try {
            viewModelScope.launch {
                settingsRepository.setMode(mode)
            }
        } catch (e: Exception){
            setError(SettingsError.FailedToPersist)
        }
    }

    open fun updatePitchMargin(marginMidi: Int){
        try {
            viewModelScope.launch {
                settingsRepository.setPitchDisplayMargin(marginMidi)
            }
        } catch (e: Exception){
            setError(SettingsError.FailedToPersist)
        }
    }

    protected fun setError(error: SettingsError){
        _error.value = error
    }

    protected fun clearError(){
        _error.value = null
    }
}

/**
 * Represents possible errors that can occur during settings changes
 */
sealed interface SettingsError {

    /**
     * Produced when rangeMin is set >= rangeMax. Ranges < 1 are not possible, as all instruments
     * are capable of producing at least one pitch.
     */
    data object RangeLessThanOne : SettingsError

    /**
     * Represents a failure to save data to the settings repository
     */
    data object FailedToPersist : SettingsError
}