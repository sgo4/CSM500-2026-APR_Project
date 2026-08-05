package com.sawears.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.withFrameNanos
import com.sawears.algorithm_development.pitch.PitchResult
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.ln
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.sawears.model.pitch.PitchEstimator
import com.sawears.data.io.audioIn.AudioInputSource

@Composable
fun TestUI(){
}

/**
 * Convert Hz values (logarithmic) to midi (linear) for
 * more intuitive visual representation
 *
 * @param pitchHz
 * @return approximate midi value (may not be a whole number)
 */
fun hzToMidi(pitchHz: Float): Float {
    return 69f + 12f * (ln(pitchHz / 440f) / ln(2f))
}

/**
 * Convert midi value to y-axis position on screen.
 *
 * Used in representing time, frequency as cartesian coordinates
 *
 * @param pitchMidi
 * @param minMidi
 * @param maxMidi
 * @param height the height of the screen / canvas / graph
 */
fun midiToY(
    pitchMidi: Float,
    minMidi: Float,
    maxMidi: Float,
    height: Float
): Float {
    val normalized = ((pitchMidi - minMidi) / (maxMidi - minMidi))
        .coerceIn(0f, 1f)

    // Higher pitches appear at the top of the graph.
    return height * (1f - normalized)
}

