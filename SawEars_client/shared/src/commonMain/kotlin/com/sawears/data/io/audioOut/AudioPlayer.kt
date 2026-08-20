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