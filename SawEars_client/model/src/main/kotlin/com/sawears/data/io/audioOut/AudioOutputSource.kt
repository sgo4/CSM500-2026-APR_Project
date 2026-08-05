package com.sawears.data.io.audioOut

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
     * Read a frame of samples into an audio processing buffer.
     *
     * @return the number of samples written, or 0 if source is empty
     */
    fun readIntoBuffer(buffer: ShortArray): Int

}