package com.sawears.ui.screens.lesson

import kotlin.math.log2
import kotlin.math.abs


/**
 * Calculates pitch accuracies for notes currently in view
 *
 *
 */
class AccuracyTracker {

    // the idea here is to only record note accuracies for notes which are currently in view
    // rather than trying to mainntain all notes in memory
    val noteAccuracies: ArrayDeque<NoteAccuracy> = ArrayDeque()

    // TODO kdoc
    fun update(
        notesInView: List<DisplayNote>,
        currentTimeBeats: Float,
        isOnPitch: Boolean
    ){
        clearOutdatedNotes(notesInView)
        val currentNote = notesInView
            .firstOrNull { it.isCurrentNote }
            ?: return // do nothing if no note is current note

        check(currentTimeBeats >= currentNote.startsOnBeat){
            "Current beats must be >= start of current note"
        }

        val currentNoteAccuracy = noteAccuracies
            .firstOrNull { it.idx == currentNote.idx}
            ?: NoteAccuracy(
                    idx = currentNote.idx,
                    lastUpdatedBeats = currentNote.startsOnBeat
                ).also {
                noteAccuracies.add(it)
            }

        // increase % accuracy if on pitch
        if (isOnPitch){
            val beatsDelta = currentTimeBeats - currentNoteAccuracy.lastUpdatedBeats
            val percentDelta = beatsDelta / currentNote.durationRelative
            currentNoteAccuracy.percentOnPitch += percentDelta
        }
        currentNoteAccuracy.lastUpdatedBeats = currentTimeBeats
    }

    /*
    Discard outdated entries which are too far in the past to be displayed anymore
     */
    private fun clearOutdatedNotes(notesInView: List<DisplayNote>){
        val oldestNoteIdx = notesInView.firstOrNull()?.idx ?: 0
        while (noteAccuracies.isNotEmpty() && noteAccuracies[0].idx < oldestNoteIdx){
            noteAccuracies.removeFirst()
        }
    }

    /**
     * Calculates whether measured pitch is sufficiently accurate vs expected pitch based
     * on defined threshold.
     *
     * Implementation notes: rests are always considered on pitch, and are defined as any note below 0f
     * by convention.
     *
     * @param currentNoteHz the current note in the melody
     * @param measuredPitchHz the pitch of the latest measured pitch sample
     * @param thresholdCents the allowable cents difference between current note and measured pitch for
     * user to be considered "on pitch"
     */
    fun isOnPitch(currentNoteHz: Float, measuredPitchHz: Float, thresholdCents: Float): Boolean {
        val centsDifference = centsDifference(measuredPitchHz,currentNoteHz)

        if (currentNoteHz < 0f){
            return true // notes below 0f are considered rests by convention and are always "on pitch"
        } else {
            return centsDifference < thresholdCents
        }
    }

    /*
    Calculate cents difference between two pitches
     */
    private fun centsDifference(measured: Float, actual: Float): Float {
        return abs(1200 * log2((actual/measured)))
    }

    fun reset(){
        noteAccuracies.clear()
    }

}


data class NoteAccuracy(
    val idx: Int,
    var lastUpdatedBeats: Float,
    var percentOnPitch: Float = 0f
)