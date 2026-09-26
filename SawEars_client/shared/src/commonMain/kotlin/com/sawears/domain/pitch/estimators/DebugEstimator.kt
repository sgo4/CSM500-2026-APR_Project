package com.sawears.model.pitch.estimators

import com.sawears.data.io.audioIn.AudioFrame
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.pitch.PitchResult


/**
 * Temporary estimator class for debug only
 * //TODO remove from final build
 */
class DebugEstimator(
    override val sampleRate: Float,
    override val frameSize: Int,
    private val frequencyHz: Double = 523.0 // = 72 midi
): PitchEstimator {

    override fun estimate(frame: AudioFrame): PitchResult {


        return PitchResult(
            frame.frameId,
            frequencyHz + (-3..3).random(), // give some natural variance
            System.nanoTime()
        )
    }
}