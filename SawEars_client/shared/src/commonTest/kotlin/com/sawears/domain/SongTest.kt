package com.sawears.domain


import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.Test

class SongTest {


    private data class TranspositionTestCase(
        val melody: FloatArray,
        val rangeMin: Int,
        val rangeMax: Int,
        val transposedMelody: FloatArray
    )
    /**
     * Tests that a Song can be correctly transposed to fit within a given range
     *
     * Tests both odd and even ranges.
     */
    @Test
    fun `transpose song to fit target range`(){

        val cases = listOf(
            TranspositionTestCase(
                floatArrayOf(1f,2f,3f),
                9,
                11,
                floatArrayOf(9f,10f,11f)
            ),
            TranspositionTestCase(
                floatArrayOf(1f,2f,3f,4f,5f,6f,7f,8f,9f,10f),
                20,
                40,
                floatArrayOf(25f,26f,27f,28f,29f,30f,31f,32f,33f,34f)
            ),
            TranspositionTestCase(
                floatArrayOf(1f,2f,3f,4f,5f,6f,7f,8f,9f,10f),
                21,
                41,
                floatArrayOf(26f,27f,28f,29f,30f,31f,32f,33f,34f,35f)
            ),
            TranspositionTestCase(
                floatArrayOf(2f,4f,6f,8f),
                1,
                10,
                floatArrayOf(2f,4f,6f,8f)
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
            floatArrayOf(1f,1000f) // song with a really big range!
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
            floatArrayOf(1f,2f) // song with a really big range!
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
            floatArrayOf(1f,2f)
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
fun generateTestSong(midiValues: FloatArray): Song {

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