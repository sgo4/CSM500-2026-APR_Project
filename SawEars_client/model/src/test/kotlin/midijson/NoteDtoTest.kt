package com.sawears.model.data.midijson

import com.sawears.model.Song.Note
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.math.round

class NoteDtoTest {

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
}