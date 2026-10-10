package com.sawears

import com.sawears.data.midijson.SongRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.ui.screens.lesson.components.PlaybackClock
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.settings.DifficultySettingsProvider

/**
 * Common interface for dependency injection container
 */
interface AppContainer {

    /**
     * Stores global settings
     */
    val settingsRepository: SettingsRepository

    /**
     * Repository for local lesson-scoped settings
     */
    val lessonSettingsRepository: LessonSettingsRepository

    /**
     * Class that loads a song from memory
     */
    val songRepository: SongRepository

    /**
     * Factory for plaform specific [com.sawears.data.io.audioOut.AudioPlayer]
     */
    val audioPlayerFactory: AudioPlayerFactory

    /**
     * Factory to provide source of audio input, e.g. from microphone
     */
    val audioInputSourceFactory: AudioInputSourceFactory

    /**
     * Pitch estimation
     */
    val estimator: PitchEstimator

    /**
     * Factory for app database
     */
    val db: AppDatabase

    /**
     * Converts difficulty levels to concrete application settings
     */
    val difficultySettingsProvider: DifficultySettingsProvider

    /**
     * Tracks latency for real time processing. For production, a no op version should be
     * supplied.
     */
    val latencyTracker: RealTimeLatencyTracker

}
