package com.sawears.data.io.audioOut

/**
 * Interface for an audio player which converts PCM values to sound output
 * on a given platform.
 */
interface AudioPlayer {

    /**
     * A source of PCM samples
     */
    val audioSource: AudioOutputSource

    /**
     * Write a window of audio to be played
     *
     * @return number of bytes written
     */
    fun writeWindow(): Int

    /**
     * Start / restart playback
     */
    fun play()

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Discard buffered audio
     */
    fun flush()

    /**
     * Stop playback and discard unplayed audio
     */
    fun stop()

	/**
	 * Close the player (platform specific implementation)
	 */
	fun close()
}

class MockAudioPlayer(): AudioPlayer {
    override val audioSource: AudioOutputSource
        get() = TODO("Not yet needed")

    override fun writeWindow(): Int {
        return 0
    }

    override fun play() {
       // do nothing
    }

    override fun pause() {
        // do nothing
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