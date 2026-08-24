package com.sawears

import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.settings.EphemeralLocalSettingsRepository
import com.sawears.data.settings.LocalSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.useCases.LoadSong
import com.sawears.ui.screens.lesson.components.PlaybackClock
import javax.inject.Inject

/**
 * Android dependency injection container, for use with hilt framework
 */
class AndroidAppContainer
@Inject constructor (
    override val settingsRepository: SettingsRepository,
    override val localSettingsRepository: LocalSettingsRepository,
    override val loadSong: LoadSong,
    override val audioPlayer: AudioPlayer,
    override val estimator: PitchEstimator,
    override val audioInputSourceFactory: AudioInputSourceFactory,
    override val playbackClock: PlaybackClock,
    override val db: AppDatabase
): AppContainer