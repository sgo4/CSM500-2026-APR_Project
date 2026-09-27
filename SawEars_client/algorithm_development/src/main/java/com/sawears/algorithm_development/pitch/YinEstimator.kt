package com.sawears.algorithm_development.pitch

import be.tarsos.dsp.pitch.FastYin
import be.tarsos.dsp.pitch.Yin
import com.sawears.algorithm_development.pitch.writeCsv
import com.sawears.algorithm_development.io.AudioFrame

/**
 * Yin estimator based on the TarsosDSP Fast Yin implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 *
 * @author Joren Six
 */
class FastYinEstimator(
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {
    private val estimator = FastYin(sampleRate, frameSize)
    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStamp)
    }
}


/**
 * Yin estimator based on the TarsosDSP Yin implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 *
 * @author Joren Six
 */
class YinEstimator(
    override val sampleRate: Float,
    override val frameSize: Int
): PitchEstimator {
    private val estimator = Yin(sampleRate, frameSize)
    override fun estimate(frame: AudioFrame): PitchResult {
        return PitchResult(estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStamp)
    }
}