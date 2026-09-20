package com.sawears.mocks

import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MockSettingsRepository(
    appSettings: AppSettings = AppSettings()
): SettingsRepository {

    private val _settings = MutableStateFlow(appSettings)
    override val settings: StateFlow<AppSettings> = _settings

    override suspend fun setDifficulty(difficulty: Difficulty) {
        _settings.update { it.copy(difficulty = difficulty) }
    }
    override suspend fun setRange(rangeMinMidi: Int, rangeMaxMidi: Int) {
        _settings.update {
            it.copy(
                rangeMinMidi = rangeMinMidi,
                rangeMaxMidi = rangeMaxMidi
            )
        }
    }

    override suspend fun setMode(mode: LessonMode) {
        _settings.update {
            it.copy(
                mode=mode
            )
        }
    }

    override suspend fun setPitchDisplayMargin(marginMidi: Int) {
        _settings.update {
            it.copy(
                pitchDisplayMarginMidi = marginMidi
            )
        }
    }
}