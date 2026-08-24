package com.sawears.data.settings

import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.Flow


interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setRangeMinMidi(pitchMidi: Int)
    suspend fun setRangeMaxMidi(pitchMidi: Int)
    suspend fun setDifficulty(difficulty: Int)
    suspend fun setMode(mode: LessonMode)

}
