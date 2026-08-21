package com.sawears

import com.sawears.data.MidiJsonRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.database.AppDatabaseFactory
import com.sawears.data.database.getDatabaseBuilder
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioIn.WindowsMicAudioInputSource
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.io.audioOut.WindowsAudioPlayer
import com.sawears.ui.lesson.components.PlaybackClock
import com.sawears.model.pitch.estimators.FixedSineEstimator
import com.sawears.model.useCases.LoadSong



/**
 * Windows dependency injection container. For now manual DI is used
 * as dependencies are fairly simple.
 */
class WindowsAppContainer private constructor(): AppContainer {

    override val loadSong: LoadSong = LoadSong(
        MidiJsonRepository()
    )

    override val audioPlayer = WindowsAudioPlayer(
        SineWaveOutputSource()
    )

    //    override val estimator = FastYinEstimator(
//        44_100f,
//        1024
//    )
    override val estimator = FixedSineEstimator(
        44_100f,
        1024,
        frequency = 2f,
        minPitcHz = 600,
        maxPitchHz = 1000
    )

    override val audioInputSourceFactory = AudioInputSourceFactory {
        WindowsMicAudioInputSource()
    }

    override val playbackClock = PlaybackClock()

    override val databaseFactory = AppDatabaseFactory()

    private lateinit var _db: AppDatabase

    val db: AppDatabase
        get() = _db

    private suspend fun initialize() {
        _db = databaseFactory.create(getDatabaseBuilder())
    }

    companion object {
        suspend fun create(): WindowsAppContainer {
            val container = WindowsAppContainer()
            container.initialize()
            return container
        }
    }
}