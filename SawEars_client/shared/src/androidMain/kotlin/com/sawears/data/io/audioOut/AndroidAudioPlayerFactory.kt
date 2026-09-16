package com.sawears.data.io.audioOut

/**
 * Factory for android specific [AudioPlayer] implementation
 */
class AndroidAudioPlayerFactory: AudioPlayerFactory {
    override fun create(): AudioPlayer {
        return AndroidAudioPlayer(
            audioSource = SineWaveOutputSource()
        )
    }
}