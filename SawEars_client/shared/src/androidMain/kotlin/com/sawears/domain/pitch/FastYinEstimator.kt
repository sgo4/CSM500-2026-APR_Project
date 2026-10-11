package com.sawears.domain.pitch

import be.tarsos.dsp.pitch.FastYin
import com.sawears.data.io.audioIn.AudioWindow

/**
 * Yin estimator based on the TarsosDSP Fast Yin implementation
 * Adapted to use common interface
 * Source: https://github.com/JorenSix/TarsosDSP/
 *
 * @author Joren Six
 * @author Soeren Otter-Sharp
 * @property sampleRate samples per second in Hz
 * @property windowSize the size of audio window in n_samples
 */
class FastYinEstimator(
    override val sampleRate: Float,
    override val windowSize: Int
): PitchEstimator {
    private val estimator = FastYin(sampleRate, windowSize)
    override fun estimate(frame: AudioWindow): PitchResult {
        return PitchResult(frame.frameId,estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStampNanos)
    }
}