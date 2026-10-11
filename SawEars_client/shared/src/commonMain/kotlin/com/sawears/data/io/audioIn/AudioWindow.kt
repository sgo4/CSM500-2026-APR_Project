package com.sawears.data.io.audioIn

/**
 * Represents a single window of audio samples
 *
 * @param frameId
 * @param samples an array of normalised PCM samples
 * @param timeStampNanos timestamp captured
 */
class AudioWindow(
    val frameId: Long,
    val samples: FloatArray,
    val timeStampNanos: Long
)