package com.sawears.data.io.audioOut

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem


import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine


/**
 * Windows implementation of audio player class
 */
class WindowsAudioPlayer(
    override val audioSource: AudioOutputSource
): AudioPlayer {

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
    private var _line: SourceDataLine? = null
    val internalBufferSize = byteWindow.size * 4

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
        _line?.start()
    }

    override fun pause(){
        _line?.stop()
    }

    override fun flush() {
        _line?.flush()
    }

    override fun stop(){
        _line?.flush()
        _line?.stop()
    }

    private fun drain() {
        _line?.drain()
    }

    /*
    Write contents of byte window to audio line
     */
    private fun writeToLine(
        bytes: ByteArray,
        length: Int
    ) {
        val line = getAudioLine()

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

    /*
    Get open line or initialize one (allows for lazy initialization)
     */
    private fun getAudioLine(): SourceDataLine {
        _line?.let{ return it }

        val line = AudioSystem.getSourceDataLine(format)
        line.open(format, internalBufferSize)
        _line = line
        return line
    }

    override fun close(){
        _line?.drain()
        _line?.stop()
        _line?.close()
    }
}