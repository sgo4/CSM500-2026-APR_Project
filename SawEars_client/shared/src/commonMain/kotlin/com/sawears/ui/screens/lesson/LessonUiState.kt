package com.sawears.ui.screens.lesson

import com.sawears.model.Song
import com.sawears.model.pitch.PitchResult

/**
 * State holder for main lesson screen
 *
 * @property song current song around which lesson is based
 * @property notesInView which notes are currently withing display window
 * @property playBackHeadBeats position of the playback head within song *
 * @property paused lesson play/pause state (for animations)
 * @property audioPaused audio playback play/pause state
 * @property recordAudio whether audio recording is enabled
 * @property pendingPitchSamples samples which have been received but not yet processed
 */
data class LessonUiState(
    val song: Song,
    val tempoBpm: Int,
    val notesInView: List<DisplayNote> = emptyList(),
    val playBackHeadBeats: Float = 0f,
    val playBackHeadPosition: Float = 0.5f,
    val canvasWidthBeats: Float = 5f,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val recordAudio: Boolean = false, // TODO this isn't really ui state
    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
)