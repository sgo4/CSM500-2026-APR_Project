package com.sawears.domain

import com.sawears.domain.songModel.Note
import com.sawears.domain.songModel.TranspositionException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.math.round


class NoteTest {

    private data class MsConverstionTestCase(
        val relativeDuration: Double,
        val tempo: Int,
        val expectedMs: Double
    )

    @Test
    fun `converstion to ms behaves as expected`(){

        val cases = listOf(
            MsConverstionTestCase(
                1.0,
                120,
                500.0
            ),
            MsConverstionTestCase(
                0.5,
                180,
                166.67
            )
        )

        cases.forEach { case ->
            val note = Note(
                60f,
                case.relativeDuration
            )
            val msDuration = note.toMs(case.tempo)
            val rounded = round(msDuration*100) / 100

            assertEquals(case.expectedMs,rounded)

        }

    }

    private data class MidiHzTestCase(
        val midiPitch: Float,
        val expectedHz: Float
    )

    @Test
    fun `correctly converts midi to hz`(){

        val cases = listOf(
            MidiHzTestCase(
                60f,
                261.63f
            ),
            MidiHzTestCase(
                69f,
                440f
            ),
            MidiHzTestCase(
                78f,
                739.99f
            ),
            MidiHzTestCase(
                127f,
                12543.86f
            ),
            MidiHzTestCase(
                0f,
                8.18f
            ),
            MidiHzTestCase(
                -1f,
                -1f
            )
        )

        cases.forEach { case ->
            val midiNote = Note(
                pitchMidi = case.midiPitch,
                durationRelative = 1.5
            )
            val pitchHz = midiNote.toHz()
            assertEquals(case.expectedHz, round(pitchHz * 100) / 100)
        }
    }


    private data class TranspositionTestCase(
        val pitchMidi: Float,
        val semitones: Int,
        val transposedPitch: Float
    )

    @Test
    fun `transposition works with valid values`(){

        val cases = listOf(
            TranspositionTestCase(
                60f,
                1,
                61f
            ),
            TranspositionTestCase(
                60f,
                -1,
                59f
            ),
            TranspositionTestCase(
                70f,
                -10,
                60f
            )
        )

        cases.forEach{ case ->
            val note = Note(
                case.pitchMidi,
                1.0
            )

            val transposedNote = note.transpose(case.semitones)
            assertEquals(case.transposedPitch,transposedNote.pitchMidi)
        }

    }

    @Test
    fun `transposition rejects negative values`(){
        val note = Note(
            0f,
            1.0
        )

        assertFailsWith<TranspositionException>{
            note.transpose(-1)
         }
    }
}
