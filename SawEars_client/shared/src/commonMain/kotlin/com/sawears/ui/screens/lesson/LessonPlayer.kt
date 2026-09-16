package com.sawears.ui.screens.lesson

import com.sawears.model.Note
import com.sawears.ui.screens.lessonsettings.LessonMode

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
                        note.pitchMidi,
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
     * Implementation note: This is where performance vs. practice mode affects playback.
     * If PERFORMANCE mode is selected, the timeline always runs while playing.
     * If PRACTICE mode is selected, the timeline only advances if user is currently onPitch
     * or if the current note is a rest
     *
     * @param frameDeltaMillis time elapsed since last frame in milliseconds
     * @param mode determines behaviour of next frame
     * @param isOnPitch whether user is currently "on pitch" (affects animation progress)
     */
    fun nextFrame(
        frameDeltaMillis: Float,
        mode: LessonMode,
        isOnPitch: Boolean,
    ): PlaybackSnapshot {

        val currentNote = currentNote()
        val advanceTimeline: Boolean = when {
            // timeline should advance continuously in performance mode
            mode == LessonMode.PERFORMANCE -> true
            // timeline should only progress if there is a valid note
            currentNote == null -> false
            // by convention, pitches below 0f indicate a rest, so timeline
            // should always progress
            currentNote.pitchMidi < 0f -> true
            // advance timeline if note is on pitch
            else -> isOnPitch
        }
        if (advanceTimeline) {
            playBackHeadBeats += frameDeltaMillis / (60_000f / tempoBpm)
        }

        return PlaybackSnapshot(
            playBackHeadBeats,
            notesInView()
        )
    }

    /**
     * Exposes the current note
     *
     * @return the current note in the melody
     */
    fun currentNote(): Note? = song.notes.getOrNull(currentNoteIdx)
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