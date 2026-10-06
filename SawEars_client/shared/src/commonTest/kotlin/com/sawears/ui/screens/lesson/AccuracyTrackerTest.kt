package com.sawears.ui.screens.lesson

import com.sawears.domain.pitch.midiToHz
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class AccuracyTrackerTest{

    @Test
    fun `updates note accuracy`(){

        val tracker = AccuracyTracker()
        var notesInView = mutableListOf(
            DisplayNote(
                2,
                60f,
                1f,
                1f,
                true
            ),
            DisplayNote(
                3,
                60f,
                1f,
                2f,
                false
            )
        )

        val isOnPitch = tracker.isOnPitch(
            currentNoteHz=midiToHz(notesInView.get(0).pitchMidi),
            measuredPitchHz=261.63f, // 261.63 hz = 60 midi, i.e. should count as accurate
            thresholdCents=50f
        )

        assertTrue(isOnPitch)

        // simulate updating note accuracy 50% into note duration
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats = 1.5f,
            isOnPitch
        )
        // pitch should now have 50% accuracy
        assertEquals(
            0.5f,tracker.noteAccuracies[0].percentOnPitch
        )
    }

    @Test
    fun `on pitch calculations respect threshold`(){

        val tracker = AccuracyTracker()

        val notesInView = listOf(
            DisplayNote(
                1,
                60f,
                1f,
                0f,
                true
            )
        )
        var isOnPitch = tracker.isOnPitch(
            currentNoteHz=midiToHz(notesInView.get(0).pitchMidi),
            measuredPitchHz=1000f, // way too high, should be inaccurate
            50f
        )

        assertFalse(isOnPitch)

        // update with an inaccurate pitch and check that percent accuracy remains 0
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            isOnPitch
        )
        var percentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertEquals(0f,percentAccuracy)

        isOnPitch = tracker.isOnPitch(
            currentNoteHz=midiToHz(notesInView.get(0).pitchMidi),
            measuredPitchHz=269f, // 48 cents difference, should pass with 50 threshold
            50f
        )
        assertTrue(isOnPitch)
        // update with values that should pass
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.2f,
            isOnPitch
        )

        var newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertTrue {
            newPercentAccuracy > percentAccuracy
        }
        percentAccuracy = newPercentAccuracy

        isOnPitch = tracker.isOnPitch(
            currentNoteHz=midiToHz(notesInView.get(0).pitchMidi),
            measuredPitchHz=255f, // 44 cents difference, should pass with 50 threshold
            50f
        )
        assertTrue(isOnPitch)

        // try another value that should pass (this time too low)
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.4f,
            isOnPitch
        )

        newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertTrue {
            newPercentAccuracy > percentAccuracy
        }
        percentAccuracy = newPercentAccuracy

        // try another value that should fail
        isOnPitch = tracker.isOnPitch(
            currentNoteHz=midiToHz(notesInView.get(0).pitchMidi),
            measuredPitchHz=254f, // 51 cents difference, should pass with 50 threshold
            50f
        )
        assertFalse(isOnPitch)

        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            isOnPitch
        )
        newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertEquals(percentAccuracy, newPercentAccuracy)

    }
}