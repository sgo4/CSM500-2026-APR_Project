package com.sawears.ui.screens.lesson

import com.sawears.domain.songModel.Note
import com.sawears.domain.songModel.Song
import com.sawears.mocks.mockSong
import kotlin.test.Test
import kotlin.test.assertEquals
import com.sawears.ui.screens.lessonsettings.LessonMode
import junit.framework.TestCase.assertTrue
import kotlin.math.abs
import kotlin.test.assertNotNull


/**
 * These tests validate the behaviour of the lesson player,
 * the core class responsible for advancing the timeline
 * and animation state of the piano roll style display.
 *
 * These tests relate to the following objectives:
 * - O2: A user interface representing expected vs actual pitch
 *  using a side-scrolling piano roll style display
 * - O5: A 'wait' mode that allows the user to reach the expected
 *  pitch before moving to the next note, ignoring tempo
 */
class LessonPlayerTests {

    @Test
    fun `Lesson player correctly identifies notes in frame`(){
        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                3.0,
                1.0,
                3.0,
                1.0
            )
        )

        val player = LessonPlayer(
            lessonState= LessonUiState(song,60, LessonMode.PERFORMANCE)
        )

        val frameDeltaMillis = 4000f

        val notesInView = player
            .nextFrame(
                frameDeltaMillis,
                mode=LessonMode.PERFORMANCE,
                isOnPitch=true)
            .notesInView


        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(3, notesInView.size)
        assertEquals(1f,notesInView[1].durationRelative)
        assertEquals(4f,notesInView[1].startsOnBeat)
    }

    @Test
    fun `Lesson player correctly identifies notes in first frame`(){
        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                3.0,
                1.0,
                3.0,
                1.0
            )
        )

        val player = LessonPlayer(LessonUiState(song, 60, LessonMode.PERFORMANCE))

        val notesInView = player.nextFrame(
            0f,
            mode=LessonMode.PERFORMANCE,
            isOnPitch=true
        ).notesInView

        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(2, notesInView.size)
        assertEquals(3f,notesInView[1].durationRelative)
        assertEquals(1f,notesInView[1].startsOnBeat)
    }

    @Test
    fun `Lesson player correctly identifies notes in last frame`(){
        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                1.0,
                3.0,
                1.0
            )
        )

        val player = LessonPlayer(LessonUiState(song, 60,LessonMode.PERFORMANCE))

        val frameDeltaMillis = 5000f

        val notesInView = player
            .nextFrame(
                frameDeltaMillis,
                LessonMode.PERFORMANCE,
                true)
            .notesInView

        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(2, notesInView.size)
        assertEquals(1f,notesInView[1].durationRelative)
        assertEquals(5f,notesInView[1].startsOnBeat)
    }

    /**
     * While performance mode active, time should progress regardless of whether user is
     * on pitch.
     *
     * To test this we call nextFrame with both true and false values for isOnPitch and verify
     * that the timeline advances.
     */
    @Test
    fun `Performance mode advances timeline regardless of user pitch`(){

        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                3.0,
                1.0,
                3.0,
                1.0
            )
        )
        val mode = LessonMode.PERFORMANCE
        val player = LessonPlayer(
            lessonState= LessonUiState(song,60, mode)
        )

        // initial timestamp
        val t1 = player.playBackHeadBeats

        // request new frame while on pitch
        player.nextFrame(
            frameDeltaMillis=500f,
            mode=mode,
            isOnPitch = true
        )

        // time should have progressed
        val t2 = player.playBackHeadBeats
        assertTrue(t2 > t1)

        // request new frame while not on pitch
        player.nextFrame(
            frameDeltaMillis=500f,
            mode=mode,
            isOnPitch = false
        )

        // time should have progressed
        val t3 = player.playBackHeadBeats
        assertTrue(t3 > t2)
    }

    /**
     * While practice mode is active, the timeline only advance while the user is on pitch,
     * to give the user sufficient chance to practice the correct note.
     *
     * To test this we call nextFrame with both true and false values for isOnPitch and verify
     * that the timeline only advances in the true case.
     *
     * This test partially validates objective O5
     */
    @Test
    fun `Practice mode only advances timeline when user is on pitch`(){

        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                3.0,
                1.0,
                3.0,
                1.0
            )
        )
        val mode = LessonMode.PRACTICE
        val player = LessonPlayer(
            lessonState= LessonUiState(song,60, mode)
        )

        // initial timestamp
        val t1 = player.playBackHeadBeats

        // request new frame while on pitch
        player.nextFrame(
            frameDeltaMillis=500f,
            mode=mode,
            isOnPitch = true
        )

        // time should have progressed
        val t2 = player.playBackHeadBeats
        assertTrue(t2 > t1)

        // request new frame while not on pitch
        player.nextFrame(
            frameDeltaMillis=500f,
            mode=mode,
            isOnPitch = false
        )

        // time should not have progressed
        val t3 = player.playBackHeadBeats
        assertTrue(abs(t3 - t2) < 0.0001) // account for Float imprecision

    }

    /**
     * Verifies that changing tempo affects how quickly notes scroll across
     * the screen. Strictly speaking this tests the playback state from which
     * animations are derrived.
     *
     * This is tested by advancing playback by 1000 ms at various tempos and
     * measuring how many notes through a fixed melody the note advances.
     *
     * The Fixed melody consists of 4 beats of pitches 1, 2, 3, 4 for ease of analysis.
     *
     * At 60 bpm the animation state should advance from beat 1 to beat 2
     * At 120 bpm the animation state should advance from beat 1 to beat 3
     * At 180 bpm the animation state should advance from beat 1 to beat 4
     */
    @Test
    fun `Tempo affects animation speed`(){

        val testCases = listOf(
            PlayBackTempoTestCase(
                tempo=60,
                expectedNote = 2
            ),
            PlayBackTempoTestCase(
                tempo=120,
                expectedNote = 3
            ),
            PlayBackTempoTestCase(
                tempo=180,
                expectedNote = 4
            )
        )

        val mode = LessonMode.PERFORMANCE // ensure timeline should always advance

        testCases.forEach{ case ->

            val tempo = case.tempo
            val song = mockSong(
                pitches=listOf(1,2,3,4),
                durations=listOf(1,1,1,1)
            )

            val player = LessonPlayer(
                lessonState= LessonUiState(
                    song=song,
                    tempoBpm=tempo,
                    mode=mode,
                )
            )

            // advance by 1 second
            val snapshot = player.nextFrame(
                frameDeltaMillis = 1000f,
                mode=mode,
                isOnPitch=true
            )

            val currentNote = snapshot.notesInView.filter { it.isCurrentNote }.firstOrNull()
            assertEquals(currentNote!!.pitchMidi, case.expectedNote.toFloat() )
        }
    }
}



/*
Helper function to generate test songs with specified durations
without specifying any other details
 */
fun generateTestSong(durations: DoubleArray): Song {

    val notes = mutableListOf<Note>()

    for (duration in durations){
        notes.add(
            Note(
                60f,
                duration
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

data class PlayBackTempoTestCase(
    val tempo: Int,
    val expectedNote: Int
)