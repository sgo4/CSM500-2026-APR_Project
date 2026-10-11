package com.sawears.data.io.audioIn

/**
 * Generic interface for audio source as a stream of normalized PCM samples
 */
interface AudioInputSource: AutoCloseable {

    /**
     * The number of samples in each output [AudioWindow]
     */
    val windowSize: Int

    /**
     * The sample rate in Hz
     */
    val sampleRate: Int

    /**
     * Reads a single  [AudioWindow] of [windowSize] samples from device audio source
     */
    fun readWindow(): AudioWindow?
}