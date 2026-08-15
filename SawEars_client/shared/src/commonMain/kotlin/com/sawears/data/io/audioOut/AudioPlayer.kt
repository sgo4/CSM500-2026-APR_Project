package com.sawears.data.io.audioOut

interface AudioPlayer {
    val audioSource: AudioOutputSource

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
     * Play unplayed audio
     */
    fun drain()
	
	/**
	 * Close the player (platform specific implementation)
	 */
	fun close()
}