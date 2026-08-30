package com.sawears.ui.screens.lesson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AccuracyTrackerTest{

    @Test
    fun `clears outdates notes from view`(){
        val tracker = AccuracyTracker()

        var notesInView = mutableListOf(
            DisplayNote(
                1,
                60f,
                1f,
                0f,
                true
            ),
            DisplayNote(
                2,
                60f,
                1f,
                1f,
                false
            ),
            DisplayNote(
                3,
                60f,
                1f,
                2f,
                false
            )
        )

        // update tracker
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats = 0f,
            currentPitchHz = 400f
        )
        // check that entry is created for current note in view
        assertEquals(1, tracker.noteAccuracies.size)

        // simulate time progressing so that note 1 is no longer in view
        notesInView =  mutableListOf(
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

        tracker.update(
            notesInView=notesInView,
            currentTimeBeats = 1f,
            currentPitchHz = 400f
        )
        // check that the size of noteAccuracies does not increase
        assertEquals(1, tracker.noteAccuracies.size)
        // check that tracker now holds data for note idx 2
        assertEquals(2,tracker.noteAccuracies[0].idx)
    }


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

        // simulate updating note accuracy 50% into note duration
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats = 1.5f,
            currentPitchHz = 261.63f // 261.63 hz = 60 midi, i.e. should count as accurate
        )
        // pitch should now have 50% accuracy
        assertEquals(
            0.5f,tracker.noteAccuracies[0].percentOnPitch
        )
    }

    @Test
    fun `on pitch calculations respect threshold`(){

        val tracker = AccuracyTracker(
            thresholdCents = 50f
        )

        val notesInView = listOf(
            DisplayNote(
                1,
                60f,
                1f,
                0f,
                false
            )
        )

        // update with an inaccurate pitch and check that percent accuracy remains 0
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            currentPitchHz=1000f, // way too high, should be inaccurate
        )
        var percentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertEquals(0f,percentAccuracy)

        // update with values that should pass
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            currentPitchHz=410f, // 42 cents difference, should pass with 50 threshold
        )

        var newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertTrue {
            newPercentAccuracy > percentAccuracy
        }
        percentAccuracy = newPercentAccuracy

        // try another value that should pass
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            currentPitchHz=411f, // 46 cents difference, should pass with 50 threshold
        )

        newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertTrue {
            newPercentAccuracy > percentAccuracy
        }
        percentAccuracy = newPercentAccuracy

        // try another value that should fail
        tracker.update(
            notesInView=notesInView,
            currentTimeBeats=0.1f,
            currentPitchHz=412f, // 51 cents difference, should fail with 50 threshold
        )
        newPercentAccuracy = tracker.noteAccuracies[0].percentOnPitch
        assertEquals(percentAccuracy, newPercentAccuracy)

    }
}