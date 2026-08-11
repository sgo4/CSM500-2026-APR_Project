package com.sawears.desktop

import com.sawears.data.MidiJsonRepository
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.io.audioOut.WindowsAudioPlayer
import com.sawears.model.useCases.LoadSong

interface AppContainer {
    val loadSong: LoadSong
    val audioPlayer: AudioPlayer
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
}