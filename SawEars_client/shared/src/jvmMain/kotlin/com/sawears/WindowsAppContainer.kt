package com.sawears

import com.sawears.data.MidiJsonRepository
import com.sawears.data.SongRepository
import com.sawears.data.database.buildWindowsDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioIn.WindowsMicAudioInputSource
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.io.audioOut.WindowsAudioPlayer
import com.sawears.data.settings.DataStoreSettingsRepository
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.createDataStore
import com.sawears.model.pitch.estimators.DebugEstimator
import com.sawears.ui.screens.lesson.components.PlaybackClock
import com.sawears.model.pitch.estimators.FixedSineEstimator



/**
 * Windows dependency injection container. For now manual DI is used
 * as dependencies are fairly simple.
 */
class WindowsAppContainer(): AppContainer {

    override val settingsRepository = DataStoreSettingsRepository(
        createDataStore()
    )

    override val lessonSettingsRepository: LessonSettingsRepository
        = EphemeralLessonSettingsRepository()

    override val songRepository: SongRepository = MidiJsonRepository()

    override val audioPlayer = WindowsAudioPlayer(
        SineWaveOutputSource()
    )

    //    override val estimator = FastYinEstimator(
//        44_100f,
//        1024
//    )
//    override val estimator = FixedSineEstimator(
//        44_100f,
//        1024,
//        frequency = 2f,
//        minPitcHz = 600,
//        maxPitchHz = 1000
//    )
    override val estimator = DebugEstimator(44_100f,1024)

    override val audioInputSourceFactory = AudioInputSourceFactory {
        WindowsMicAudioInputSource()
    }

    override val playbackClock = PlaybackClock()

    override val db = buildWindowsDatabase()
}