package com.sawears.algorithm_development.pitch

import be.tarsos.dsp.pitch.McLeodPitchMethod
import com.sawears.algorithm_development.io.AudioFrame


/**
 * McLeod estimator based on the TarsosDSP implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 * @author Joren Six
 * @author Soeren Otter-Sharp (interface wrapper only)
 */
class McLeodEstimator(
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {
    private val estimator = McLeodPitchMethod(sampleRate, frameSize)
    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStamp)
    }
}