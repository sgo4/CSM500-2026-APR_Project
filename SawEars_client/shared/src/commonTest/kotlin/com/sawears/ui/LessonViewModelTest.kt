package com.sawears.ui
import com.sawears.model.Note
import com.sawears.model.Song
import kotlin.test.Test
import kotlin.test.assertEquals
import com.sawears.ui.lesson.LessonPlayer


class LessonViewModelTest {

    @Test
    fun `Lesson correctly identifies notes in frame`(){
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
            song,
            60,
            5f,
            0.5f
        )

        player.systemTimeMillis = 0
        val currentTime = 4000L

        val notesInView = player.notesInView(
            currentTime
        )

        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(3, notesInView.size)
        assertEquals(1f,notesInView[1].durationRelative)
        assertEquals(4f,notesInView[1].startsOnBeat)
    }

    @Test
    fun `Lesson correctly identifies notes in first frame`(){
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
            song,
            60,
            5f,
            0.5f
        )

        player.systemTimeMillis = 0L
        val currentTime = 0L

        val notesInView = player.notesInView(
            currentTime
        )

        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(2, notesInView.size)
        assertEquals(3f,notesInView[1].durationRelative)
        assertEquals(1f,notesInView[1].startsOnBeat)
    }

    @Test
    fun `Lesson correctly identifies notes in last frame(s)`(){
        val song = generateTestSong(
            doubleArrayOf(
                1.0,
                1.0,
                3.0,
                1.0
            )
        )

        val player = LessonPlayer(
            song,
            60,
            5f,
            0.5f
        )

        player.systemTimeMillis = 0L
        val currentTime = 5000L

        val notesInView = player.notesInView(
            currentTime
        )

        for (note in notesInView){
            println(note.durationRelative)
        }

        assertEquals(2, notesInView.size)
        assertEquals(1f,notesInView[1].durationRelative)
        assertEquals(5f,notesInView[1].startsOnBeat)
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
                60,
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