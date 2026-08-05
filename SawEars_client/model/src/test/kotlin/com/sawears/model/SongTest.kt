package com.sawears.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.Test

class SongTest {

    /**
     * Tests that a Song can be correctly transposed to fit within a given range
     */
    @ParameterizedTest
    @MethodSource("validTranspositionTestCases")
    fun `transpose song to fit target range`(
        melody: IntArray,
        rangeMin: Int,
        rangeMax: Int,
        transposedMelody: IntArray
    ){

        val song = generateTestSong(melody)
        val transposedSong = song.adaptForRange(rangeMin, rangeMax)
        val expected = generateTestSong(transposedMelody)

        assertEquals(expected,transposedSong)

    }

    @Test
    fun `range transposition fails if target range smaller than song range`(){

        val song = generateTestSong(
            intArrayOf(1,1000) // song with a really big range!
        )

        val rangeMin = 2
        val rangeMax = 4 // really small target range!

        assertFailsWith<TranspositionException> {
            song.adaptForRange(rangeMin, rangeMax)
        }
    }

    @Test
    fun `range transposition fails with negative target range`(){

        val song = generateTestSong(
            intArrayOf(1,2) // song with a really big range!
        )

        val rangeMin = 20
        val rangeMax = 2 // max less than min

        assertFailsWith<TranspositionException> {
            song.adaptForRange(rangeMin, rangeMax)
        }
    }

    @Test
    fun `range transposition fails with negative range boundaries`(){

        val song = generateTestSong(
            intArrayOf(1,2) // song with a really big range!
        )

        val rangeMin = -1 // negative range
        val rangeMax = 20

        assertFailsWith<TranspositionException> {
            song.adaptForRange(rangeMin, rangeMax)
        }
    }

    /*
     * Test cases in form notes, rangeMin, rangeMax, transposed notes
     */
    companion object {
        @JvmStatic
        fun validTranspositionTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(intArrayOf(1,2,3),9,11,intArrayOf(9,10,11)),
            Arguments.of(intArrayOf(1,6,10),20,40, intArrayOf(26,31,35)),
            Arguments.of(intArrayOf(2,4,6,8),1,10,intArrayOf(2,4,6,8))
        )
    }
}

/*
Helper function to generate test songs with specified note values
without specifying any other details
 */
fun generateTestSong(midiValues: IntArray): Song {

    val notes = mutableListOf<Note>()

    for (midi in midiValues){
        notes.add(
            Note(
                midi,
                1.0
            )
        )
    }

    return Song(
        "title",
        listOf("composer"),
        null,
        null,
        null,
        notes
    )
}