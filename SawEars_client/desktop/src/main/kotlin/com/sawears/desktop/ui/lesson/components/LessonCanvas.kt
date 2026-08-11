package com.sawears.desktop.ui.lesson.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.desktop.ui.lesson.DisplayNote
import com.sawears.desktop.ui.lesson.LessonUiState
import com.sawears.desktop.ui.lesson.LessonViewModel
import kotlin.math.ceil
import kotlin.math.ln


/**
 * Canvas for piano roll style lesson
 *
 * @param viewModel
 * @param durationSeconds time range to display
 * @param pitchDisplayMarginMidi margin around song's range to display, as a %
 */
@Composable
fun lessonCanvas(
    viewModel: LessonViewModel,
    durationSeconds: Double,
    pitchDisplayMarginMidi: Float = 5f
){

    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()

    LaunchedEffect(lessonState.paused) {
        if (!lessonState.paused) {
            var previousFrameNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (previousFrameNanos != 0L){
                        val frameDeltaMillis = (frameNanos - previousFrameNanos) / 1_000_000
                        viewModel.updateClock(frameDeltaMillis)
                    }
                    previousFrameNanos = frameNanos
                }
            }
        }
    }

    Canvas(Modifier.fillMaxSize()){
        val player = lessonState.player?: return@Canvas

        // set up canvas dimensions
        val canvasWidthPx = size.width
        val canvasHeightPx = size.height

        if (canvasWidthPx <= 0f || canvasHeightPx <= 0f) {
            return@Canvas
        }

        // calculate range of pitches to display based on song range +/- a fixed margin
        val minPitchMidi = player.song.songMinMidi - pitchDisplayMarginMidi
        val maxPitchMidi = player.song.songMaxMidi + pitchDisplayMarginMidi

        // combine dimension-related information into single object
        val dimensions = LessonCanvasDimensions(
            minPitchMidi,
            maxPitchMidi,
            canvasWidthPx,
            canvasHeightPx
        )
        // canvas height must be >= range of displayable notes otherwise they won't fit on screen
        if (canvasHeightPx < (maxPitchMidi-minPitchMidi)){
            return@Canvas
        }

        drawOctaveBoundaries(
            dimensions
        )
        drawPianoRoll(
            lessonState,
            dimensions
        )
    }
}

fun DrawScope.drawPianoRoll(
    lessonState: LessonUiState,
    dimensions: LessonCanvasDimensions
){
    val player = lessonState.player?: return
    val notesInView = player.notesInView(lessonState.frameMillis)
    // calculate noteheight as canvas height / range of displayable notes
    val noteHeightPx = dimensions.canvasHeightPx / (dimensions.maxPitchMidi - dimensions.minPitchMidi)
    // calculate startX
    val headPx = player.playBackHeadPosition * dimensions.canvasWidthPx
    val pxPerBeat = dimensions.canvasWidthPx / player.canvasWidthBeats

    for (note in notesInView){

        val distanceFromHeadBeats = note.startsOnBeat - player.playBackHeadBeats
        val startX = headPx + (distanceFromHeadBeats * pxPerBeat)
        val noteWidthPx = note.durationRelative * pxPerBeat
        drawNote(
            note,
            dimensions,
            startX,
            noteHeightPx,
            noteWidthPx
        )
    }
}

fun DrawScope.drawNote(
    note: DisplayNote,
    dimensions: LessonCanvasDimensions,
    startX: Float,
    noteHeight: Float,
    noteWidth: Float,
){

    val colour = if (note.isCurrentNote) {Color(0xFFFFF08D)
    } else {Color(0xFF8DC0FF)}
    val centreY = midiToY(note.pitchMidi, dimensions)

    drawRoundRect(
        colour,
        topLeft = Offset(startX,centreY+(noteHeight/2)),
        size = Size(noteWidth,noteHeight),
        cornerRadius = CornerRadius(
            x = noteHeight / 2f,
            y = noteHeight / 2f
        )

    )
}

/**
 * Generate octave dividers for pitch display
 *
 * @param dimensions the dimensions of the canvas
 * @param colour (colour of midi lines on screen)
 */
fun DrawScope.drawOctaveBoundaries(
    dimensions: LessonCanvasDimensions,
    colour: Color = Color(0xFF343A40)
){


    val firstMidiLine = ceil(dimensions.minPitchMidi / 12f).toInt() * 12

    var midiLine = firstMidiLine

    while(midiLine <= dimensions.maxPitchMidi){
        val yCoord = midiToY(
            midiLine.toFloat(),
            dimensions
        )

        drawLine(
            colour,
            Offset(0f,yCoord),
            Offset(dimensions.canvasWidthPx, yCoord),
            strokeWidth = 1f
        )

        midiLine += 12 // note: 12 semitones in an octave
    }
}

fun DrawScope.drawCurrentPitch(
    pitchMidi: Float,
    dimensions: LessonCanvasDimensions
){


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
 * @param dimensions the dimensions of the canvas
 */
fun midiToY(
    pitchMidi: Float,
    dimensions: LessonCanvasDimensions
): Float {
    val normalized = ((pitchMidi - dimensions.minPitchMidi) / (dimensions.maxPitchMidi - dimensions.minPitchMidi))
        .coerceIn(0f, 1f)

    // Higher pitches appear at the top of the graph.
    return dimensions.canvasHeightPx * (1f - normalized)
}

fun startBeatToStartX(startBeat: Float, headBeat: Float, headPx: Float, canvasWidth: Float, canvasWidthBeats: Float): Float{

    val distanceFromHeadBeats = startBeat - headBeat
    val pxPerBeat = canvasWidthBeats / canvasWidth

    return headPx * (distanceFromHeadBeats * pxPerBeat)
}

/**
 * Object to store lesson canvas dimensions required by multiple draw functions
 *
 * @param minPitchMidi
 * @param maxPitchMidi
 * @param canvasWidthPx
 * @param canvasHeightPx
 */
data class LessonCanvasDimensions(
    val minPitchMidi: Float,
    val maxPitchMidi: Float,
    val canvasWidthPx: Float,
    val canvasHeightPx: Float
)