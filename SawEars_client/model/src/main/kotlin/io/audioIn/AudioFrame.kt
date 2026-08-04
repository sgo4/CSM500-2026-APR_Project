package com.sawears.model.io.audioIn

/**
 * A single frame of audio
 *
 * @param samples an array of normalised PCM samples
 * @param timeStampNanos time captured
 */
class AudioFrame(
    val samples: FloatArray,
    val timeStampNanos: Double
)