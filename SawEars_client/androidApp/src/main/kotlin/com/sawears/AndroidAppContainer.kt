package com.sawears

import com.sawears.data.midijson.SongRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.settings.DifficultySettingsProvider
import com.sawears.ui.screens.lesson.components.PlaybackClock
import javax.inject.Inject

/**
 * Android dependency injection container, for use with hilt framework
 */
class AndroidAppContainer
@Inject constructor (
    override val settingsRepository: SettingsRepository,
    override val lessonSettingsRepository: LessonSettingsRepository,
    override val songRepository: SongRepository,
    override val audioPlayerFactory: AudioPlayerFactory,
    override val estimator: PitchEstimator,
    override val audioInputSourceFactory: AudioInputSourceFactory,
    override val playbackClock: PlaybackClock,
    override val db: AppDatabase,
    override val difficultySettingsProvider: DifficultySettingsProvider,
    override val latencyTracker: RealTimeLatencyTracker
): AppContainer