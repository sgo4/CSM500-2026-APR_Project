package com.sawears

import com.sawears.data.midijson.MidiJsonRepository
import com.sawears.data.midijson.SongRepository
import com.sawears.data.database.buildWindowsDatabase
import com.sawears.data.diagnostics.WindowsDiagnosticsCsvWriter
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioIn.JvmMicAudioSource
import com.sawears.data.io.audioOut.WindowsAudioPlayerFactory
import com.sawears.data.settings.DataStoreSettingsRepository
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.createDataStore
import com.sawears.diagnostics.DiagnosticRealTimeLatencyTracker
import com.sawears.diagnostics.NoOpRealTimeLatencyTracker
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.domain.pitch.FastYinEstimator
import com.sawears.domain.pitch.McLeodEstimator
import com.sawears.domain.settings.StandardDifficultySettingsProvider


/**
 * Windows dependency injection container. For now manual DI is used
 * as dependencies are fairly simple.
 */
class WindowsAppContainer(): AppContainer {

    override val settingsRepository = DataStoreSettingsRepository(
        createDataStore()
    )

    override val lessonSettingsRepository: LessonSettingsRepository =
        EphemeralLessonSettingsRepository()

    override val songRepository: SongRepository = MidiJsonRepository()

    override val audioInputSourceFactory = AudioInputSourceFactory {
        JvmMicAudioSource()
    }

    override val audioPlayerFactory = WindowsAudioPlayerFactory()

    override val estimator = McLeodEstimator(
        44_100f,
        1024
    )

    override val db = buildWindowsDatabase()

    override val difficultySettingsProvider = StandardDifficultySettingsProvider()

    override val latencyTracker: RealTimeLatencyTracker = NoOpRealTimeLatencyTracker()
}

/**
 * Separate container for running live diagnostics tests
 */
class WindowsDiagnosticAppContainer(): AppContainer {

    override val settingsRepository = DataStoreSettingsRepository(
        createDataStore()
    )

    override val lessonSettingsRepository: LessonSettingsRepository =
        EphemeralLessonSettingsRepository()

    override val songRepository: SongRepository = MidiJsonRepository()

    override val audioInputSourceFactory = AudioInputSourceFactory {
        JvmMicAudioSource()
    }

    override val audioPlayerFactory = WindowsAudioPlayerFactory()

    override val estimator = McLeodEstimator(
        44_100f,
        1024
    )
    override val db = buildWindowsDatabase()

    override val difficultySettingsProvider = StandardDifficultySettingsProvider()

    override val latencyTracker: RealTimeLatencyTracker = DiagnosticRealTimeLatencyTracker(
        WindowsDiagnosticsCsvWriter(fileName="latencyDiagnostics.csv")
    )

}
