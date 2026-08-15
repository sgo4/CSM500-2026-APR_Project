package com.sawears.data.io.audioIn

/**
 * Generic interface for audio source as a stream of normalized PCM samples
 * @property frameSize the size of the output frame in n_samples
 * @property sampleRate in Hz
 */
interface AudioInputSource {
    val frameSize: Int
    val sampleRate: Int
    fun readFrame(): AudioFrame?
    fun close()
}