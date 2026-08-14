package com.sawears.desktop

import com.sawears.model.pitch.estimators.FastYinEstimator
import com.sawears.data.MidiJsonRepository
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.data.io.audioIn.WindowsMicAudioInputSource
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.io.audioOut.WindowsAudioPlayer
import com.sawears.desktop.ui.lesson.components.PlaybackClock
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.pitch.estimators.FixedSineEstimator
import com.sawears.model.useCases.LoadSong

interface AppContainer {
    val loadSong: LoadSong
    val audioPlayer: AudioPlayer
    val estimator: PitchEstimator
    val audioInputSource: AudioInputSource
    val playbackClock: PlaybackClock
}

/**
 * Simple dependency injection container to supply classes to application.
 *
 * Could be replaced with more advanced DI through hilt etc. but not necessary at
 * this stage as dependencies are fairly simple.
 */
class DefaultAppContainer : AppContainer {
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
        frequency=2f,
        minPitcHz = 600,
        maxPitchHz = 1000
    )

    override val audioInputSource = WindowsMicAudioInputSource()

    override val playbackClock = PlaybackClock()
}