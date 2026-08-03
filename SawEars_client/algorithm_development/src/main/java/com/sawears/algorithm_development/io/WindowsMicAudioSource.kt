package com.sawears.algorithm_development.io

import java.io.Closeable
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.math.max
import javax.sound.sampled.Mixer

/**
 * Windows specific implementation of AudioSource
 * Used for laptop based experiments
 */
class WindowsMicAudioSource(
    override val frameSize: Int = 1024,
    override val sampleRate: Int = 44_100,
    mixerName: String? = null
) : AudioSource, Closeable {

    init {
        require(frameSize > 0) {
            "frameSize must be greater than zero"
        }

        require(sampleRate > 0) {
            "sampleRate must be greater than zero"
        }
    }

    val samples = FloatArray(frameSize)

    /*
        Audio format for PCM signed 16 bit mono little endian (for windows testing)
     */
    private val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        sampleRate.toFloat(),
        16,     // bits per sample
        1,      // mono
        2,      // bytes per frame
        sampleRate.toFloat(),
        false   // little-endian
    )

    private val byteBuffer = ByteArray(frameSize * 2)

    private val line: TargetDataLine = openMicrophoneLine(mixerName)

    @Volatile
    private var closed = false

    init {
        val minimumBufferBytes = frameSize * 2
        val requestedBufferBytes = max(minimumBufferBytes * 4, 4096)

        line.open(format, requestedBufferBytes)
        line.start()
    }

    /*
     * Read a single frame of audio from the mic with the desired size.
     * Blocks until one complete frame has been captured.
     */
    override fun readFrame(): AudioFrame? {
        if (closed) return null

        var offset = 0

        while (offset < byteBuffer.size) {

            if (closed) return null

            val count = line.read(
                byteBuffer,
                offset,
                byteBuffer.size - offset
            )

            if (count < 0) {
                return null
            }

            if (count == 0) {
                continue
            }

            offset += count

        }

        for (sampleIndex in 0 until frameSize) {
            val byteIndex = sampleIndex * 2

            val low = byteBuffer[byteIndex].toInt() and 0xFF
            val high = byteBuffer[byteIndex + 1].toInt()

            // Signed 16-bit little-endian PCM.
            val pcm16 = (high shl 8) or low

            samples[sampleIndex] = pcm16 / 32768.0f
        }

        // Copy so previously returned frames are not overwritten by the next read.
        return AudioFrame(samples.copyOf(), System.nanoTime().toDouble())
    }

    /**
     * Close the connection to the mic
     */
    override fun close() {
        if (closed) return
        closed = true

        line.stop()
        line.flush()
        line.close()
    }
    /*
     Open a microphone line using either a specified mixer name of the defaul mixer

     @param mixerName the desired mixer name, or null to use default
     */
    private fun openMicrophoneLine(mixerName: String?): TargetDataLine {

        val lineInfo = DataLine.Info(
            TargetDataLine::class.java,
            format
        )

        if (mixerName == null) {
            check(AudioSystem.isLineSupported(lineInfo)) {
                "The default microphone does not support $format"
            }

            return AudioSystem.getLine(lineInfo) as TargetDataLine
        }

        val mixerInfo = AudioSystem.getMixerInfo().firstOrNull {
            it.name.contains(mixerName, ignoreCase = true)
        } ?: throw IllegalArgumentException(
            "No audio mixer matching '$mixerName' was found"
        )

        val mixer = AudioSystem.getMixer(mixerInfo)

        check(mixer.isLineSupported(lineInfo)) {
            "Mixer '${mixerInfo.name}' does not support $format"
        }

        return mixer.getLine(lineInfo) as TargetDataLine
    }

    /**
     * Search available microphones to find one that supports the desired format
     * (in our case signed 16 bit little endian PCM samples)
     */
    fun findCaptureMixer(format: AudioFormat): Mixer.Info {
        val requestedLine = DataLine.Info(
            TargetDataLine::class.java,
            format
        )

        return AudioSystem.getMixerInfo()
            .firstOrNull { mixerInfo ->
                val mixer = AudioSystem.getMixer(mixerInfo)
                mixer.isLineSupported(requestedLine)
            }
            ?: error("No microphone supports $format")
    }
}