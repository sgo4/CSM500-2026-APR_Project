package com.sawears.data.settings

import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository for app wide settings
 */
interface SettingsRepository {
    val settings: Flow<AppSettings>

    /**
     * Set minimum range, used in calculating transpositions
     *
     * @param pitchMidi
     */
    suspend fun setRangeMinMidi(pitchMidi: Int)

    /**
     * Set max range, used in calculating transpositions
     *
     * @param pitchMidi
     */
    suspend fun setRangeMaxMidi(pitchMidi: Int)

    /**
     * Set difficulty for Lesson settings
     *
     * @param difficulty
     */
    suspend fun setDifficulty(difficulty: Int)

    /**
     * Set lesson mode
     *
     * Implementation note:
     * Enum needs to be converted to string for storage. The reverse conversion
     * occurs when dataStore values are read into settings.
     *
     * @param mode enum value representing defaul lesson mode
     */
    suspend fun setMode(mode: LessonMode)

    /**
     * Set display margin
     *
     * @property marginMidi how many notes to display either side of song range
     */
    suspend fun setPitchDisplayMargin(marginMidi: Int)

}
