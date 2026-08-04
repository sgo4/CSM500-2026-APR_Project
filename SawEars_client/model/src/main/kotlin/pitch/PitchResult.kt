package com.sawears.model.pitch

/**
 * Model layer representation of the result of a pitch estimation for a single
 * frame of audio.
 *
 * @property frequencyHz in Hz
 * @property timeStampNanos time at which estimate was captured
 * @property confidence model confidence (where applicable)
 */
class PitchResult (
    val frequencyHz: Double?,
    val timeStampNanos: Double,
    val confidence: Double? = null
)
