package com.sawears.data.io.audioOut

import be.tarsos.dsp.pitch.FastYin
import com.sawears.data.io.audioIn.AudioWindow
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.pitch.PitchResult
import com.sawears.domain.songModel.Song
import com.sawears.mocks.mockSong
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioOutTests {

    /**
     * Tests that correct length of PCM stream generated, sensitive
     * to tempo and note duration.
     *
     * A single beat at 48000hz should produce 24k samples
     * at 120 bpm and 48k samples at 60 bpm
     */
    @Test
    fun `output source produces expected number of samples`(){

        val testCases = listOf(
            SampleCountTestCase(
                song = mockSong(
                    pitches = listOf(1),
                    durations = listOf(1),
                    tempo=120
                ),
                sampleRate = 48_000,
                expectedSamples = 24_000
            ),
            SampleCountTestCase(
                song = mockSong(
                    pitches = listOf(1),
                    durations = listOf(1),
                    tempo=60
                ),
                sampleRate = 48_000,
                expectedSamples = 48_000
            )
        )

        testCases.forEach{ testCase ->
            val outputSource = SineWaveOutputSource(sampleRate = testCase.sampleRate)
            outputSource.loadInput(testCase.song)

            // buffer of known size
            val buffer = ShortArray(1048)
            var countRead = 0

            // read samples into buffer until exhausted, keeping
            // track of samples read
            while (true) {
                val samplesRead = outputSource.readIntoBuffer(buffer)

                countRead += samplesRead
                if (samplesRead < 1){
                    break
                }
            }
            assertEquals(
                testCase.expectedSamples,
                countRead
            )
        }


    }

    /**
     * Tests that the PCM stream generated corresponds to the expected
     * frequency
     *
     * E.g. MIDI 69 -> 440hz
     *
     * Rests, represented by negative MIDI values should produce a period of
     * silence
     *
     * Implementation note: Relies on applying pitch estimation (YIN) which
     * has a natural margin of error, so equivalence is +/- 0.5 hz
     */
    @Test
    fun `output source produces expected pitch`(){
        val testCases = listOf(
            PitchOutputTestCase(
                song = mockSong(
                    pitches = listOf(69) // 440 hz
                ),
                expectedFrequencyHz = 440.0
            ),
            PitchOutputTestCase(
                song = mockSong(
                    pitches = listOf(-1) // rest value
                ),
                expectedFrequencyHz = Double.NaN // pitch should be silent
            )
        )
        val sampleRate = 48_000f
        val estimator = TestEstimator(
            sampleRate = sampleRate,
            windowSize = 1024
        )
        testCases.forEach{ testCase ->
            val outputSource = SineWaveOutputSource(sampleRate = 48_000)
            outputSource.loadInput(testCase.song)

            // buffer same size as estimator framesize
            val buffer = ShortArray(estimator.windowSize)
            // holder for pitch estimates
            val estimates = mutableListOf<Double>()
            // read samples into buffer until exhausted, tracking pitch
            while (true) {
                val samplesRead = outputSource.readIntoBuffer(buffer)
                if (samplesRead < 1){
                    break
                }
                val pitchEstimate = estimator.estimate(
                    AudioWindow(
                        frameId = 0,
                        samples = buffer.map {
                            it.toFloat()
                        }.toFloatArray(),
                        timeStampNanos = 0L
                    )
                ).frequencyHz

                pitchEstimate?.let{
                    if (it > 0) { // ignore silent frames
                        estimates += it
                    }
                }
            }

            // rests should produce empty list of estimates
            // since estimates < 0 are filtered out in loop above
            if (testCase.expectedFrequencyHz.isNaN()) {
                assertTrue(estimates.isEmpty())
            } else {
                assertTrue{
                    // average recorded frequency should be within 0.5 hz of target
                    abs(estimates.average() - testCase.expectedFrequencyHz) < 0.5
                }
            }
        }
    }
}

data class SampleCountTestCase(
    val song: Song,
    val sampleRate: Int,
    val expectedSamples: Int
    )
data class PitchOutputTestCase(
    val song: Song,
    val expectedFrequencyHz: Double
)

/**
 * Pitch estimator used for verifying PCM output
 */
class TestEstimator(
    override val sampleRate: Float,
    override val windowSize: Int
): PitchEstimator {
    private val estimator = FastYin(sampleRate, windowSize)
    override fun estimate(frame: AudioWindow): PitchResult {
        return PitchResult(frame.frameId,estimator.getPitch(frame.samples).pitch.toDouble(), frame.timeStampNanos)
    }
}