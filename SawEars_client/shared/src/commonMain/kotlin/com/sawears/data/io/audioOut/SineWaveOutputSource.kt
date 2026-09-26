package com.sawears.data.io.audioOut

import com.sawears.domain.Song
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Provides a stream of PCM samples for audio output
 * Serves as platform independent input for platform
 * dependent sound generation.
 *
 * @param defaultTempo tempo to be used if none specified by input
 * @param amplitude controls volume (float in range -1 <= amplitude <= 1)
 * @property sampleRate samples per second
 * @property attackReleaseMillis transition period between notes to avoid jumps (ms)
 */
class SineWaveOutputSource(
    var currentInput: Song? = null,
    defaultTempo: Int = 90,
    override val sampleRate: Int = 48_000,
    private val amplitude: Float = 0.25f,
    private val attackReleaseMillis: Double = 5.0
): AudioOutputSource {

    private var tempo = defaultTempo
    private var samples: ShortArray? = null
    private var offset = 0

    init {
        require(tempo > 0){
            "Tempo must be an integer greater than 0"
        }
        require(amplitude in -1f..1f){
            "Amplitude must be in range -1 <= amplitude <= 1"
        }
    }

    override fun loadInput(input: Song){
        currentInput = input
        tempo = currentInput!!.tempo?: tempo
        reset()
    }

    override fun reset(){
        samples = sinePCMStream(currentInput!!)
        offset = 0
    }

    override fun readIntoBuffer(buffer: ShortArray): Int{
        if (samples == null){
            return 0
        }
        val windowSize = buffer.size
        if (offset >= samples!!.size){
            return 0
        }

        val count = minOf(windowSize,(samples!!.size - offset))

        for (idx in 0 until count){
            buffer[idx] = samples!![offset]
            offset++
        }

        return count
    }

    /**
     * Generate a signed 16 bit PCM sample stream for a sine wave at correct hz values
     */
    private fun sinePCMStream(input: Song): ShortArray{
        val notes = input.notes
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

}