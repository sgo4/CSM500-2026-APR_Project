package com.sawears.ui.screens.lesson

/**
 * Player class that controlls the main animations
 * //TODO update kdoc
 */
class LessonPlayer(
    lessonState: LessonUiState
){

    val playBackHeadPosition = lessonState.playBackHeadPosition
    val canvasWidthBeats = lessonState.canvasWidthBeats
    val song = lessonState.song
    val tempoBpm = lessonState.tempoBpm
    var playBackHeadBeats: Float = 0f
    var systemTimeMillis: Long? = null
    var noteTransitions = FloatArray(song.notes.size)
    var currentNoteIdx = 0
    var currentWindowStartBeats = 0f
    var currentWindowEndBeats = currentWindowStartBeats + canvasWidthBeats
    var latestConsumedNoteIdx = -1
    var consumedNotesBeats = 0f
    val pastBeats = canvasWidthBeats * playBackHeadPosition
    val futureBeats = canvasWidthBeats * (1-playBackHeadPosition)


    init {
        // populate noteTransitions with the end of each note
        var boundary = 0f
        for (i in noteTransitions.indices){
            boundary += song.notes[i].durationRelative.toFloat()
            noteTransitions[i] = boundary
        }
    }

    /**
     * Calculate which notes are in view (fully or partially) at current time
     * based on current position of playback head
     */
    fun notesInView(): List<DisplayNote>{

        while (currentNoteIdx < noteTransitions.size && playBackHeadBeats >= noteTransitions[currentNoteIdx]){
            currentNoteIdx ++ // update current note idx
        }
        // calculate window start / end in terms of cummulative beats elapsed
        currentWindowStartBeats = playBackHeadBeats - pastBeats
        currentWindowEndBeats = playBackHeadBeats + futureBeats

        var windowStartIdx = latestConsumedNoteIdx
        var cummulativeBeats = maxOf(consumedNotesBeats,0f)
        while (windowStartIdx < song.notes.size-1 && cummulativeBeats <= currentWindowStartBeats){
            windowStartIdx++
            cummulativeBeats += song.notes[windowStartIdx].durationRelative.toFloat()
        }
        windowStartIdx = maxOf(windowStartIdx,0)
        latestConsumedNoteIdx = windowStartIdx - 1
        consumedNotesBeats = maxOf(cummulativeBeats - song.notes[windowStartIdx].durationRelative.toFloat(), 0f)

        val res = mutableListOf<DisplayNote>()
        var noteIdx = windowStartIdx
        cummulativeBeats = consumedNotesBeats
        while (noteIdx < song.notes.size && cummulativeBeats <= currentWindowEndBeats){
            val note = song.notes[noteIdx]
            val durationRelative = note.durationRelative.toFloat()

            // by convention rests are indicated by pitch value -1
            // these should not be treated as "notes in view"
            if (note.pitchMidi >= 0){
                res.add(
                    DisplayNote(
                        idx = noteIdx,
                        note.pitchMidi.toFloat(),
                        durationRelative,
                        cummulativeBeats,
                        isCurrentNote = noteIdx == currentNoteIdx
                    )
                )
            }
            cummulativeBeats += durationRelative
            noteIdx++
        }
        return res.toList()

    }

    /**
     * Return immutable state for the next frame in the animation, based on a time delta
     *
     * @param frameDeltaMillis time elapsed since last frame in milliseconds
     */
    fun nextFrame(frameDeltaMillis: Float): PlaybackSnapshot {

        playBackHeadBeats += frameDeltaMillis / (60_000f / tempoBpm)

        return PlaybackSnapshot(
            playBackHeadBeats,
            notesInView()
        )
    }
}

/**
 * Class representing a single animation frame for the piano roll canvas,
 * @property playBackHeadBeats position of playback head within the song
 * @property notesInView which notes currently overlap with the display window
 */
data class PlaybackSnapshot(
    val playBackHeadBeats: Float,
    val notesInView: List<DisplayNote>
)

/**
 * UI layer representation of a single note including information about its
 * position in the melody
 *
 * @property idx The index of the note within the melody (serves as an id)
 * @property pitchMidi
 * @property durationRelative
 * @property startsOnBeat
 * @property isCurrentNote
 */
data class DisplayNote(
    val idx: Int,
    val pitchMidi: Float,
    val durationRelative: Float,
    val startsOnBeat: Float,
    val isCurrentNote: Boolean
)