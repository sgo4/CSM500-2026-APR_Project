package com.sawears

import com.sawears.data.MidiJsonRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.database.createDatabase
import com.sawears.data.database.getDatabaseBuilder
import com.sawears.data.io.audioIn.WindowsMicAudioInputSource
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.io.audioOut.WindowsAudioPlayer
import com.sawears.ui.lesson.components.PlaybackClock
import com.sawears.model.pitch.estimators.FixedSineEstimator
import com.sawears.model.useCases.LoadSong



/**
 * Simple dependency injection container to supply classes to application.
 *
 * Could be replaced with more advanced DI through hilt etc. but not necessary at
 * this stage as dependencies are fairly simple.
 */
class WindowsAppContainer private constructor(): AppContainer {

    // class that handles loading songs from a repository
    override val loadSong: LoadSong = LoadSong(
        MidiJsonRepository()
    )
    // audio player class
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

    override val audioInputSource = WindowsMicAudioInputSource()

    override val playbackClock = PlaybackClock()

    private lateinit var _db: AppDatabase

    override val db: AppDatabase
        get() = _db

    private suspend fun initialize() {
        _db = createDatabase(getDatabaseBuilder())
    }

    companion object {
        suspend fun create(): WindowsAppContainer {
            val container = WindowsAppContainer()
            container.initialize()
            return container
        }
    }
}