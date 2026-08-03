package com.sawears.model.Song

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.math.round

class NoteTest {

    @ParameterizedTest
    @CsvSource(
        "1,120,500",
        "0.5,180,166.67"
    )
    fun `converstion to ms behaves as expected`(relativeDuration: Double, tempo: Int, expectedMs: Double){
        val note = Note(
            60,
            relativeDuration
        )
        val msDuration = note.toMs(tempo)
        val rounded = round(msDuration*100) / 100

        assertEquals(expectedMs,rounded)
    }

    @ParameterizedTest
    @CsvSource(
        "60,261.63",
        "69,440.0",
        "78,739.99",
        "127,12543.86", // midi max
        "0,8.18", // midi min
        "-1,-1" // negative number > -1, indicating a rest
    )
    fun `correctly converts midi to hz`(midiPitch: Int,expectedHz: Float){

        val midiNote = Note(
            pitchMidi = midiPitch,
            durationRelative = 1.5
        )
        val pitchHz = midiNote.toHz()
        assertEquals(expectedHz,round(pitchHz*100) / 100)
    }

    @ParameterizedTest
    @CsvSource(
        "60,1,61",
        "60,-1,59",
        "70,-10,60",
        "0,1,1"
    )
    fun `transposition works with valid values`(pitchMidi: Int, semitones: Int, transposedPitch: Int){

        val note = Note(
            pitchMidi,
            1.0
        )

        val transposedNote = note.transpose(semitones)
        assertEquals(transposedPitch,transposedNote.pitchMidi)

    }

    @Test
    fun `transposition rejects negative values`(){
        val note = Note(
            0,
            1.0
        )

        assertFailsWith<TranspositionException>{
            note.transpose(-1)
         }

    }
}
