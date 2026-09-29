package com.sawears.domain.pitch.estimators

import com.sawears.data.io.audioIn.AudioWindow
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.pitch.PitchResult


/**
 * Temporary estimator class for debug only
 * //TODO remove from final build
 */
class DebugEstimator(
    override val sampleRate: Float,
    override val frameSize: Int,
    private val frequencyHz: Double = 523.0 // = 72 midi
): PitchEstimator {

    override fun estimate(frame: AudioWindow): PitchResult {


        return PitchResult(
            frame.frameId,
            frequencyHz + (-3..3).random(), // give some natural variance
            System.nanoTime()
        )
    }
}