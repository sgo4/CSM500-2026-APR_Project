package com.sawears.ui.screens.lesson

import com.sawears.model.Song
import com.sawears.model.pitch.PitchResult

/**
 * State holder for main lesson screen
 */
data class LessonUiState(
    val song: Song? = null,
    val player: LessonPlayer? = null,
    val paused: Boolean = true,
    val audioPaused: Boolean = true,
    val songTitle: String? = null,
    val recordAudio: Boolean = false,
    val pendingPitchSamples: ArrayList<PitchResult> = ArrayList()
)