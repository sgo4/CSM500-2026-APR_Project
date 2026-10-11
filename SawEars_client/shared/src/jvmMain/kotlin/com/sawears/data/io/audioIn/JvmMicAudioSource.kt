package com.sawears.data.io.audioIn

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.math.max

/**
 * JVM implementation of a live audio source, providing stream of signed 16-bit
 * mono PCM samples from default mic.
 *
 * Implementation notes:
 * - Internally based on TargetDataLine (https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/TargetDataLine.html)
 *  which receives binary data from a Mixer.
 */
class JvmMicAudioSource(
    override val windowSize: Int = 1024,
    override val sampleRate: Int = 44_100,
    mixerName: String? = null
) : AudioInputSource {

    init {
        require(windowSize > 0) {
            "windowSize must be greater than zero"
        }

        require(sampleRate > 0) {
            "sampleRate must be greater than zero"
        }
    }

    val samples = FloatArray(windowSize)

    private val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        sampleRate.toFloat(),
        16,     // bits per sample
        1,      // mono
        2,      // bytes per frame
        sampleRate.toFloat(),
        false   // little-endian
    )

    private val byteBuffer = ByteArray(windowSize * 2)

    private val line: TargetDataLine = openMicrophoneLine(mixerName)

    @Volatile
    private var closed = false

    private var frameId = 0L

    init {
        val minimumBufferBytes = windowSize * 2
        val requestedBufferBytes = max(minimumBufferBytes * 4, 4096)

        line.open(format, requestedBufferBytes)
        line.start()
    }

    /**
     * Reads a single window of audio of [windowSize] samples
     */
    override fun readWindow(): AudioWindow? {
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

        for (sampleIndex in 0 until windowSize) {
            val byteIndex = sampleIndex * 2

            val low = byteBuffer[byteIndex].toInt() and 0xFF
            val high = byteBuffer[byteIndex + 1].toInt()

            // Signed 16-bit little-endian PCM.
            val pcm16 = (high shl 8) or low
            // normalise samples
            samples[sampleIndex] = pcm16 / 32768.0f
        }

        // Copy so previously returned frames are not overwritten by the next read.
        return AudioWindow(frameId++, samples.copyOf(), System.nanoTime())
    }

    override fun close() {
        if (closed) return
        closed = true

        line.stop()
        line.flush()
        line.close()
    }

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
}