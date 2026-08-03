package com.sawears.algorithm_development.io

/**
 * Takes a set of samples of any size and outputs audio frames of a fixed size.
 * Can be used to serve a wav file as if it was live audio
 *
 * @param samples an array of PCM values
 * @param frameSize the desired output framesize
 */
class BufferedPCMAudioSource(
    val samples: FloatArray,
    override val frameSize: Int,
    override val sampleRate: Int
) : AudioSource{

    private var index = 0

    override fun readFrame(): AudioFrame? {
        val timeStamp = System.nanoTime().toDouble()
        if (index >= samples.size) return null

        val end = minOf(index + frameSize, samples.size)
        val frame = FloatArray(frameSize)
        samples.copyInto(
            destination = frame,
            startIndex = end-frameSize,
            endIndex = end,
            destinationOffset = 0
        )

        index = end
        return AudioFrame(frame, timeStamp)
    }
}