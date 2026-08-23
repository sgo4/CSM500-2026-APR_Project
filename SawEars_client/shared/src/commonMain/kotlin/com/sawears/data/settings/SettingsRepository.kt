package com.sawears.data.settings

import kotlinx.coroutines.flow.Flow


interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setRangeMinMidi(pitchMidi: Int)
    suspend fun setRangeMaxMidi(pitchMidi: Int)
    suspend fun setRangeMidi(minMidi: Int, maxMidi: Int)
}
