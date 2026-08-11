package com.sawears.data.io.audioOut

import java.io.Closeable
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem


interface AudioPlayer: Closeable {
    val audioSource: AudioOutputSource

    fun writeWindow(): Int

    /**
     * Start / restart playback
     */
    fun play()

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Discard buffered audio
     */
    fun flush()

    /**
     * Stop playback and discard unplayed audio
     */
    fun stop()

    /**
     * Play unplayed audio
     */
    fun drain()
}

/**
 * Windows implementation of a simple sine wave generator, for test purposes
 */
class WindowsAudioPlayer(
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

    override fun writeWindow(): Int {

        val length = audioSource.readIntoBuffer(buffer)

        require(length <= windowSize)

        var byteIdx = 0

        for (sampleIdx in 0 until length){
            val sampleValue = buffer[sampleIdx].toInt()

            byteWindow[byteIdx++] = sampleValue.toByte()
            byteWindow[byteIdx++] = (sampleValue shr 8).toByte()
        }
        writeToLine(byteWindow, byteIdx)
        return length
    }

    override fun play(){
        line.start()
    }

    override fun pause(){
        line.stop()
    }

    override fun flush() {
        line.flush()
    }

    override fun stop(){
        line.flush()
        line.stop()
    }

    override fun drain() {
        line.drain()
    }

    /*
    Write contents of byte window to audio line
     */
    private fun writeToLine(
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
            }
            offset += written
        }
    }







    override fun close(){
        line.drain()
        line.stop()
        line.close()
    }
}