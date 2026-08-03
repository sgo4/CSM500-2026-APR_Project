package com.sawears.algorithm_development.pitch

import be.tarsos.dsp.pitch.DynamicWavelet
import com.sawears.algorithm_development.io.AudioFrame

/**
 * Dynamic wavelet estimator based on the TarsosDSP implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 *
 * Based paper "Real-Time Time-Domain Pitch Tracking Using Wavelets"
 * by Eric Larson and Ross Maddox
 *
 * @author Antoine Schmitt
 * @author Joren Six
 * @author Soeren Otter-Sharp (interface wrapper only)
 */
class DynamicWaveletEstimator (
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {
    private val estimator = DynamicWavelet(sampleRate, frameSize)
    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStamp)
    }
}