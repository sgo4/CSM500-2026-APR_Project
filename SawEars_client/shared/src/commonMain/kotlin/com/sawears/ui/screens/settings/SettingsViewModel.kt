package com.sawears.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel for managing settings
 *
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun updateRangeMin(pitchMidi: Int){
        viewModelScope.launch {
            settingsRepository.setRangeMinMidi(pitchMidi)
        }
    }

    fun updateRangeMax(pitchMidi: Int){
        viewModelScope.launch {
            settingsRepository.setRangeMaxMidi(pitchMidi)
        }
    }

}