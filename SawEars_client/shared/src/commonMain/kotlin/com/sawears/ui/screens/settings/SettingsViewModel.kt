package com.sawears.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.SettingsRepository
import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel for managing settings
 *
 * Implementation Note: Open so it can be subclassed by other view models that access settings
 */
open class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )



    open fun updateRangeMin(pitchMidi: Int){
        viewModelScope.launch {
            settingsRepository.setRangeMinMidi(pitchMidi)
        }
    }

    open fun updateRangeMax(pitchMidi: Int){
        viewModelScope.launch {
            settingsRepository.setRangeMaxMidi(pitchMidi)
        }
    }

    open fun updateDifficulty(difficulty: Int){
        viewModelScope.launch {
            settingsRepository.setDifficulty(difficulty)
        }
    }

    open fun updateMode(mode: LessonMode){
        viewModelScope.launch{
            settingsRepository.setMode(mode)
        }
    }

    open fun updatePitchMargin(marginMidi: Int){
        viewModelScope.launch{
            settingsRepository.setPitchDisplayMargin(marginMidi)
        }
    }

}