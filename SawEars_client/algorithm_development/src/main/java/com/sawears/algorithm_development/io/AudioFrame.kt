package com.sawears.algorithm_development.io

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