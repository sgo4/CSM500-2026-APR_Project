package com.sawears.ui.screens.lesson

import com.sawears.model.Song
import com.sawears.model.pitch.PitchResult
import com.sawears.ui.screens.lessonsettings.LessonMode

/**
 * State holder for main lesson screen
 *
 * @property song current song around which lesson is based
 * @property tempoBpm current tempo
 * @property mode current lesson mode
 * @property notesInView which notes are currently withing display window
 * @property noteAccuracies accuracy information for notes in view
 * @property leniencyThresholdCents max allowable difference between actual and expected pitch
 * @property playBackHeadBeats position of the playback head within song
 * @property playBackHeadPosition position of playback head on screen as fraction of width
 * @property pitchDisplayMarginMidi how many notes to display around song range in midi
 * @property paused lesson play/pause state (for animations)
 * @property audioPaused audio playback play/pause state
 * @property recordAudio whether audio recording is enabled
 * @property pendingPitchSamples samples which have been received but not yet processed
 * @property durationSeconds the time window to display on screen (derived from tempo and beats on screen)
 */
data class LessonUiState(
    val song: Song,
    val tempoBpm: Int,
    val mode: LessonMode,
    val notesInView: List<DisplayNote> = emptyList(),
    val noteAccuracies: List<NoteAccuracy> = emptyList(),
    val leniencyThresholdCents: Float = 50f,
    val playBackHeadBeats: Float = 0f,
    val playBackHeadPosition: Float = 0.5f,
    val pitchDisplayMarginMidi: Float = 5f,
    val canvasWidthBeats: Float = 5f,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val recordAudio: Boolean = false, // TODO this isn't really ui state
    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
){
    val durationSeconds: Float
        get() = canvasWidthBeats * (60f / tempoBpm)
}