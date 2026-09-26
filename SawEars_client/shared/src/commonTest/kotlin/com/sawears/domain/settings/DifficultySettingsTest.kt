package com.sawears.domain.settings

import com.sawears.ui.screens.lesson.AccuracyTracker
import junit.framework.TestCase.assertFalse
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests covering the difficulty / leniency feature
 */
class DifficultySettingsTest {
    private val provider = StandardDifficultySettingsProvider()

    @Test
    fun `increasing difficulty means narrower pitch range accepted`() {
        val tracker = AccuracyTracker()
        val expectedHz = 440f

        // simulate measuring a pitch exactly on the boundary between STANDARD and HARD
        val centsDifference = provider.get(Difficulty.STANDARD).centsThreshold.toDouble()
        val measuredHz = (440.0 * 2.0.pow(centsDifference / 1200.0)).toFloat()

        // should count as on pitch for STANDARD threshold
        assertTrue(
            tracker.isOnPitch(
                expectedHz,
                measuredHz,
                provider.get(Difficulty.STANDARD).centsThreshold
            )
        )
        // should also count as on pitch for easier difficulty levels
        assertTrue(
            tracker.isOnPitch(
                expectedHz,
                measuredHz,
                provider.get(Difficulty.EASY).centsThreshold
            )
        )
        // should not count as on pitch for harder levels
        assertFalse(
            tracker.isOnPitch(
                expectedHz,
                measuredHz,
                provider.get(Difficulty.HARD).centsThreshold
            )
        )
    }

    @Test
    fun `difficulty levels are progressively harder`(){
        assertTrue(provider.get(Difficulty.EASIEST).centsThreshold > provider.get(Difficulty.EASY).centsThreshold )
        assertTrue(provider.get(Difficulty.EASY).centsThreshold > provider.get(Difficulty.STANDARD).centsThreshold )
        assertTrue(provider.get(Difficulty.STANDARD).centsThreshold > provider.get(Difficulty.HARD).centsThreshold )
        assertTrue(provider.get(Difficulty.HARD).centsThreshold > provider.get(Difficulty.HARDEST).centsThreshold )
    }

    @Test
    fun `mapping integer to difficulty level as intended`(){
        Difficulty.entries.forEach { difficulty ->
            assertEquals(difficulty, Difficulty.from(difficulty.level))
        }
    }

    @Test
    fun `invalid integer difficulties throw exception`(){
        assertFailsWith<IllegalStateException> { Difficulty.from(0) } // too small
        assertFailsWith<IllegalStateException> { Difficulty.from(Difficulty.entries.size + 1) } // too big
    }
}