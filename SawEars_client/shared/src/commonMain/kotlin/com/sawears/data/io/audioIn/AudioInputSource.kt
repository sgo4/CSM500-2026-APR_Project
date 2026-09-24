package com.sawears.data.io.audioIn

/**
 * Generic interface for audio source as a stream of normalized PCM samples
 * @property frameSize the number of samples in each output frame
 * @property sampleRate in Hz
 */
interface AudioInputSource: AutoCloseable {
    val frameSize: Int
    val sampleRate: Int
    fun readFrame(): AudioFrame?
}