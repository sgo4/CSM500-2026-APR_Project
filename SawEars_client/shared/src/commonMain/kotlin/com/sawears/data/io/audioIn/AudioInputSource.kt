package com.sawears.data.io.audioIn

/**
 * Generic interface for audio source as a stream of normalized PCM samples
 *
 * @property windowSize the number of samples in each output window
 * @property sampleRate in Hz
 */
interface AudioInputSource: AutoCloseable {
    val windowSize: Int
    val sampleRate: Int
    fun readWindow(): AudioWindow?
}