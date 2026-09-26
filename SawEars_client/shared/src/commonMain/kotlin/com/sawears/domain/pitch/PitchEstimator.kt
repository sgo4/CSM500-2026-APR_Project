package com.sawears.model.pitch

import com.sawears.data.io.audioIn.AudioFrame

/**
 * Model layer interface representing a pitch estimator
 *
 * @property sampleRate
 * @property frameSize
 */
interface PitchEstimator {

    /**
     * Sample rate in Hz (some estimators can infer this from the audio frame
     * however others require this information on initialization to
     * precompute values)
     */
    val sampleRate: Float

    /**
     * Size of the frame - again some estimators can infer this from audio frame
     * but others require this information on initialization
     */
    val frameSize: Int

    /**
     * Estimates the pitch of a single audio frame.
     *
     * @param frame A mono audio frame of signed PCM samples
     * sampled at [sampleRate].
     * @return Estimation result including estimated frequency and other information such as timestamp.
     */
    fun estimate(frame: AudioFrame): PitchResult

    /**
     * Reset the estimator enabling the object to be reused
     * Only needed for estimators that modify their internal state
     */
    fun reset(){}
}