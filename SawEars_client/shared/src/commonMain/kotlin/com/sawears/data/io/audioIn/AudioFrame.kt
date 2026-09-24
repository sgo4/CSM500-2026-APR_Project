package com.sawears.data.io.audioIn

/**
 * A single frame of audio
 *
 * @param frameId
 * @param samples an array of normalised PCM samples
 * @param timeStampNanos time captured
 */
class AudioFrame(
    val frameId: Long,
    val samples: FloatArray,
    val timeStampNanos: Long
)