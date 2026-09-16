package com.sawears.data.io.audioOut

/**
 * Factory for windows / jvm specific audio player
 */
class WindowsAudioPlayerFactory: AudioPlayerFactory {
    override fun create(): AudioPlayer{
        return WindowsAudioPlayer(
            SineWaveOutputSource()
        )
    }
}