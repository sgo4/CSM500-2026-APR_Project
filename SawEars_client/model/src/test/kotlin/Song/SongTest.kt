package com.sawears.model.Song

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.math.round

class SongTest {

    @ParameterizedTest
    @CsvSource(

    )
    fun `transpose song to fit target range`(){


    }

    companion object {
        @JvmStatic
        fun testSongs(): Stream<Arguments> = Stream.of(
            Arguments.of(),
            Arguments.of()
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