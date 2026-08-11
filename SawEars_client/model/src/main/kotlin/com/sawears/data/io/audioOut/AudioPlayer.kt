package com.sawears.data.io.audioOut

import java.io.Closeable
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem


interface AudioPlayer: Closeable {
    val audioSource: AudioOutputSource

    fun writeWindow(): Int
    fun play()
    fun pause()
    fun stop()
    fun drain()
}
/**
 * Windows implementation of a simple sine wave generator, for test purposes
 */
class WindowsSineWaveGenerator(
    override val audioSource: AudioOutputSource
): AudioPlayer, Closeable{
    val sampleRate = audioSource.sampleRate
    val windowSize = (sampleRate * 0.02).toInt() // calculate window to deliver audio in ~20 ms chunks
    private val byteWindow = ByteArray(windowSize * Short.SIZE_BYTES)
    private val buffer = ShortArray(windowSize)
    private val format = AudioFormat(
        sampleRate.toFloat(),
        16,
        1,
        true,
        false
    )
    private val line = AudioSystem.getSourceDataLine(format)
    val internalBufferSize = byteWindow.size * 4
    init {
        line.open(format, internalBufferSize)
    }

    override fun play(){
        line.start()
    }

    override fun pause(){
        line.stop()
    }

    override fun stop(){
        line.flush()
        line.stop()

    }



    override fun writeWindow(): Int {

        val length = audioSource.readIntoBuffer(buffer)

        require(length <= windowSize)

        var byteIdx = 0

        for (sampleIdx in 0 until length){
            val sampleValue = buffer[sampleIdx].toInt()

            byteWindow[byteIdx++] = sampleValue.toByte()
            byteWindow[byteIdx++] = (sampleValue shr 8).toByte()
        }
        writeFully(byteWindow, byteIdx)
        return length
    }

    private fun writeFully(
        bytes: ByteArray,
        length: Int
    ) {

        var offset = 0

        while (offset < length) {
            val written = line.write(
                bytes,
                offset,
                length - offset
            )

            check(written > 0) {
                println(
                    "Audio write failed: " +
                            "offset=$offset, length=$length, " +
                            "open=${line.isOpen}, running=${line.isRunning}, " +
                            "active=${line.isActive}, available=${line.available()}"
                )
                "Audio output wrote no data"
            }

            offset += written
        }
    }

    /**
     * Play all played audio
     */
    override fun drain() {
        line.drain()
    }

    /**
     * Discard unplayed audio
     */
    fun flush() {
        line.flush()
    }

    override fun close(){
        line.drain()
        line.stop()
        line.close()
    }
}