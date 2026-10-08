package com.sawears.domain.pitch

import be.tarsos.dsp.pitch.McLeodPitchMethod
import com.sawears.data.io.audioIn.AudioWindow

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
    override fun estimate(frame: AudioWindow): PitchResult {
        return PitchResult(
            frameId=frame.frameId,
            frequencyHz=estimator
                .getPitch(frame.samples)
                .pitch
                .toDouble(),
            timeStampNanos=frame.timeStampNanos
        )
    }
}