package com.sawears.algorithm_development.io

/**
 * Generic interface for audio source as a stream of normalized PCM samples
 * @property frameSize the size of the output frame in n_samples
 * @property sampleRate in Hz
 */
interface AudioSource {
    val frameSize: Int
    val sampleRate: Int
    fun readFrame(): AudioFrame?
}