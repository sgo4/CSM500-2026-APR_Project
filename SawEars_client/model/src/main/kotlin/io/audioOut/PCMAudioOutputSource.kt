package com.sawears.model.io.audioOut
import com.sawears.model.Song.Note
import com.sawears.model.Song.Song
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Provides a stream of PCM samples for audio output
 * Servers as platform independent input for platform
 * dependent sound generation.
 *
 * @property samples a stream of signed 16 bit PCM samples
 * @property sampleRate sample rate
 */
class PCMAudioOutputSource(
    input: Song,
    defaultTempo: Int = 90,
    val sampleRate: Float = 48_000f,
    val amplitude: Float = 0.25f,
    val attackReleaseMillis: Double = 5.0
){
    private val tempo = input.tempo?: defaultTempo
    private val notes = input.notes
    private val samples = sinePCMStream()
    private var offset = 0

    init {
        require(tempo > 0){
            "Tempo must be an integer greater than 0"
        }
    }

    /**
     * Generate a signed 16 bit PCM sample stream for a sine wave at correct hz values
     */
    private fun sinePCMStream(): ShortArray{
        val n_frames = notes.sumOf { note ->
            (note.toMs(tempo) * sampleRate / 1_000.0).toInt()
        }

        val samples = ShortArray(n_frames)

        var outputIdx = 0
        var phase = 0.0

        for (note in notes) {
            val frameCount = (
                    note.toMs(tempo) * sampleRate / 1_000L
                    ).toInt()

            val envelopeFrames =
                (attackReleaseMillis * sampleRate / 1_000.0)
                    .roundToInt()
                    .coerceAtLeast(1)
            val phaseIncrement =
                2.0 * PI * note.toHz() / sampleRate

            repeat(frameCount){frame ->
                val attack = frame.toDouble() / envelopeFrames
                val release = (frameCount - frame - 1).toDouble() / envelopeFrames

                val envelope = minOf(1.0,attack,release).coerceAtLeast(0.0)
                val sample = sin(phase) * amplitude * envelope

                samples[outputIdx++] = (sample.coerceIn(-1.0,1.0) * Short.MAX_VALUE)
                    .roundToInt()
                    .toShort()
                phase += phaseIncrement

                if (phase > 2.0 * PI){
                    phase -= 2.0 * PI
                }


                }

            }

        return samples
    }

    /**
     * Fill a given buffer with the next window of PCM samples
     *
     * @return the number of samples written
     */
    fun readIntoBuffer(destination: ShortArray): Int{
        val windowSize = destination.size
        if (offset >= samples.size){
            return 0
        }

        val count = minOf(windowSize,(samples.size - offset))

        for (idx in 0 until count){
            destination[idx] = samples[offset]
            offset++
        }

        return count
    }
}