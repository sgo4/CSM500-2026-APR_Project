package com.sawears.ui.screens.lesson


/**
 * Calculates pitch accuracies for notes currently in view
 */
class AccuracyTracker {

    val noteAccuracies: ArrayDeque<NoteAccuracy> = ArrayDeque()


    fun update(notesInView: List<DisplayNote>, currentTimeBeats: Float, onPitch: Boolean){
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

        if (onPitch){
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
        val oldestNoteIdx = notesInView.lastOrNull()?.idx ?: 0
        while (noteAccuracies.isNotEmpty() && noteAccuracies[0].idx < oldestNoteIdx){
            noteAccuracies.removeFirst()
        }
    }

}


data class NoteAccuracy(
    val idx: Int,
    var lastUpdatedBeats: Float,
    var percentOnPitch: Float = 0f
)