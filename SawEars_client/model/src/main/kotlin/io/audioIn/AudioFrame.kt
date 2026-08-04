package com.sawears.model.io.audioIn

/**
 * A single frame of audio
 *
 * @param samples an array of PCM samples
 * @param timeStamp time captured
 */
class AudioFrame(
    val samples: FloatArray,
    val timeStamp: Double
)