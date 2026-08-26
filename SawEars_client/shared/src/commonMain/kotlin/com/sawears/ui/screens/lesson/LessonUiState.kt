package com.sawears.ui.screens.lesson

import com.sawears.model.Song
import com.sawears.model.pitch.PitchResult

/**
 * State holder for main lesson screen
 */
data class LessonUiState(
    val song: Song,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val recordAudio: Boolean = false,
    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
)