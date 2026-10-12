package com.sawears.mocks

import com.sawears.data.io.audioOut.AudioOutputSource
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.domain.songModel.Song

class MockAudioPlayerFactory(): AudioPlayerFactory{
    override fun create(): AudioPlayer {
        return MockAudioPlayer()
    }

}

class MockAudioPlayer() : AudioPlayer{
    override val audioSource: AudioOutputSource
        get() = MockAudioOutputSource()

    override fun writeWindow(): Int {
        TODO("Not yet implemented")
    }

    override fun play() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}

class MockAudioOutputSource(): AudioOutputSource{
    override val sampleRate: Int
        get() = TODO("Not yet implemented")

    override fun loadInput(input: Song) {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun readIntoBuffer(buffer: ShortArray): Int {
        TODO("Not yet implemented")
    }

}