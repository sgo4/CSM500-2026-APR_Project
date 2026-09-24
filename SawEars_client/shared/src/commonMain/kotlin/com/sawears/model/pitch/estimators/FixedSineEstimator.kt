package com.sawears.model.pitch.estimators

import com.sawears.data.io.audioIn.AudioFrame
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.pitch.PitchResult
import kotlin.math.PI
import kotlin.math.sin

/**
 * A pitch estimator that generates mock results roughly following a sine wave
 * Useful for testing / debugging purposes where some usable output is needed
 * without worrying about accuracy
 */
class FixedSineEstimator(
    override val sampleRate: Float,
    override val frameSize: Int,
    private val frequency: Float = 2f,
    private val minPitcHz: Int = 600,
    private val maxPitchHz: Int = 900
): PitchEstimator {

    private val amplitude = 1.0

    override fun estimate(frame: AudioFrame): PitchResult {
        val currentTimeNanos = System.nanoTime()

        val currentTimeSeconds = currentTimeNanos / 1_000_000_000f

        val sinePosition = sin(2*PI*frequency*currentTimeSeconds)*amplitude
        val frequencyHz = sinePositionToHz(sinePosition)

        return PitchResult(
            frame.frameId,
            frequencyHz,
            currentTimeNanos
        )
    }

    /*
    Convert amplitude of the sinewave to a hz value in range provided
    @param sine the amplitude of the sine wave at given time t
     */
    private fun sinePositionToHz(sine: Double): Double {

        val minAmplitude = 0 - amplitude
        return minPitcHz + ((sine-minAmplitude)/(amplitude-minAmplitude)) * (maxPitchHz - minPitcHz)
    }
}