package com.sawears.model.pitch

import be.tarsos.dsp.pitch.FastYin
import com.sawears.data.io.audioIn.AudioFrame

/**
 * Yin estimator based on the TarsosDSP Fast Yin implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 *
 * @author Joren Six
 * @author Soeren Otter-Sharp
 */
class FastYinEstimator(
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {
    private val estimator = FastYin(sampleRate, frameSize)
    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(frame.frameId,estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStampNanos)
    }
}