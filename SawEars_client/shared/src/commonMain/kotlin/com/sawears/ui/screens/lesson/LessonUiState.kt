package com.sawears.ui.screens.lesson

import com.sawears.domain.songModel.Song
import com.sawears.ui.screens.lessonsettings.LessonMode

/**
 * State holder for main lesson screen. Holds state not directly related to frame
 * by frame recomposition.
 *
 * @property song current song around which lesson is based
 * @property tempoBpm current tempo
 * @property mode current lesson mode
 * @property leniencyThresholdCents max allowable difference between actual and expected pitch
 * @property playBackHeadPosition position of playback head on screen as fraction of width
 * @property pitchDisplayMarginMidi how many notes to display around song range in midi
 * @property paused lesson play/pause state (for animations)
 * @property audioPaused audio playback play/pause state
 * @property recordAudio whether audio recording is enabled
 * @property durationSeconds the time window to display on screen (derived from tempo and beats on screen)
 */
data class LessonUiState(
    val song: Song,
    val tempoBpm: Int,
    val mode: LessonMode,
    val leniencyThresholdCents: Float = 50f,
    val playBackHeadPosition: Float = 0.5f,
    val pitchDisplayMarginMidi: Float = 5f,
    val canvasWidthBeats: Float = 5f,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val recordAudio: Boolean = false
){
    val durationSeconds: Float
        get() = canvasWidthBeats * (60f / tempoBpm)
}

/**
 * Lesson UI state which affects frame by frame animation rendering, for use only
 * within lesson canvas
 *
 * Implementation note: The rationale for splitting canvas animation state from general
 * lesson ui state is to avoid recomposing the entire lesson every time playback advances.
 * Instead, the canvas can receive a separate state holder while the lesson only recomposes
 * on major changes
 *
 * @property playBackHeadBeats position of the playback head within song
 * @property notesInView which notes are currently withing display window
 * @property noteAccuracies accuracy information for notes in view
 */
data class CanvasAnimationState(
    val playBackHeadBeats: Float,
    val notesInView: List<DisplayNote> = emptyList(),
    val noteAccuracies: List<NoteAccuracy> = emptyList(),
//    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
)