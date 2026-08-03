package com.sawears.algorithm_development.pitch

import com.sawears.algorithm_development.io.AudioFrame

/**
 * Mock estimator that simply returns a constant
 * For testing purposes only
 */
class ConstantEstimator(
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {

    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(440.0, frame.timeStamp)
    }
}