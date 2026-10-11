package com.sawears.data.io.audioOut

import com.sawears.domain.songModel.Song

/**
 * Model layer audio output interface.
 *
 * Provides a stream of unnormalized PCM samples for consumption by a player class.
 * Intentionally platform agnostic.
 *
 * @property sampleRate samples per second
 */
interface AudioOutputSource {

    val sampleRate: Int

    /**
     * Load [Song] data from which to generate audio
     */
    fun loadInput(input: Song)
    fun reset()

    /**
     * Read a frame of samples into an audio processing buffer.
     *
     * @return the number of samples written, or 0 if source is empty
     */
    fun readIntoBuffer(buffer: ShortArray): Int

}