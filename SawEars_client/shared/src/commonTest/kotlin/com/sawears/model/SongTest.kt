package com.sawears.model


import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.Test

class SongTest {


    private data class TranspositionTestCase(
        val melody: IntArray,
        val rangeMin: Int,
        val rangeMax: Int,
        val transposedMelody: IntArray
    )
    /**
     * Tests that a Song can be correctly transposed to fit within a given range
     */
    @Test
    fun `transpose song to fit target range`(){

        val cases = listOf(
            TranspositionTestCase(
                intArrayOf(1,2,3),
                9,
                11,
                intArrayOf(9,10,11)
            ),
            TranspositionTestCase(
                intArrayOf(1,6,10),
                20,
                40,
                intArrayOf(26,31,35)
            ),
            TranspositionTestCase(
                intArrayOf(2,4,6,8),
                1,
                10,
                intArrayOf(2,4,6,8)
            )
        )

        cases.forEach{ case ->
            val song = generateTestSong(case.melody)
            val transposedSong = song.adaptForRange(case.rangeMin, case.rangeMax)
            val expected = generateTestSong(case.transposedMelody)

            assertEquals(expected,transposedSong)

        }



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