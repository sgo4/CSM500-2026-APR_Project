package com.sawears.ui.screens.lesson.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.ui.screens.lesson.CanvasAnimationState
import com.sawears.ui.screens.lesson.DisplayNote
import com.sawears.ui.screens.lesson.LessonUiState
import com.sawears.ui.screens.lesson.PitchDetectionAnimationState
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.ceil
import kotlin.math.ln



/**
 * Canvas for piano roll style lesson
 *
 * @param lessonState UI state holder containing immutable animation variables
 * @param frameRenderState frame specific animation state
 * @param PitchDetectionAnimationState holder for pitch detection specific animation state
 * @param showPlaybackHead if true, a line will be shown to indicate playback head position
 * @param showOctaveLines if true, octave dividers will be shown on screen
 */
@Composable
fun LessonCanvas(
    lessonState: LessonUiState,
    frameRenderState: StateFlow<CanvasAnimationState>,
    pitchDetectionAnimationState: State<PitchDetectionAnimationState>,
    showPlaybackHead: Boolean = true,
    showOctaveLines: Boolean = false
){

    // collect frame specific animation state
    val frameRenderState = frameRenderState.collectAsStateWithLifecycle()

    Canvas(Modifier.fillMaxSize()){

        val pitchFrame = pitchDetectionAnimationState.value

        // set up canvas dimensions
        val canvasWidthPx = size.width
        val canvasHeightPx = size.height

        if (canvasWidthPx <= 0f || canvasHeightPx <= 0f) {
            return@Canvas
        }

        // calculate range of pitches to display based on song range +/- a fixed margin
        val minPitchMidi = lessonState.song.songMinMidi - lessonState.pitchDisplayMarginMidi
        val maxPitchMidi = lessonState.song.songMaxMidi + lessonState.pitchDisplayMarginMidi
        
        // calculate how many seconds into the past to show pitch samples based on playbackhead
        val pitchCurveDurationSeconds = lessonState.durationSeconds * lessonState.playBackHeadPosition

        // combine dimension-related information into single object
        val dimensions = LessonCanvasDimensions(
            minPitchMidi,
            maxPitchMidi,
            canvasWidthPx,
            canvasHeightPx,
            lessonState.playBackHeadPosition,
            lessonState.durationSeconds,
            pitchCurveDurationSeconds
        )
        // canvas height must be >= range of displayable notes otherwise they won't fit on screen
        if (canvasHeightPx < (maxPitchMidi-minPitchMidi)){
            return@Canvas
        }

        if (showOctaveLines) drawOctaveBoundaries(
            dimensions
        )

        if (showPlaybackHead) drawPlaybackHead(
            dimensions
        )

        drawPianoRoll(
            lessonState,
            frameRenderState.value,
            dimensions
        )

        drawPitchCurve(
            pitchFrame.pitchSamples,
            dimensions,
            pitchFrame.frameNanos,
            pitchFrame.offsetNanos
        )
    }
}

/**
 * Draw function that draws the piano roll display on screen
 *
 * @param lessonState
 */
fun DrawScope.drawPianoRoll(
    lessonState: LessonUiState,
    frameRenderState: CanvasAnimationState,
    dimensions: LessonCanvasDimensions
){

    val notesInView = frameRenderState.notesInView
    // calculate noteheight as canvas height / range of displayable notes
    val noteHeightPx = dimensions.canvasHeightPx / (dimensions.maxPitchMidi - dimensions.minPitchMidi)
    // calculate startX
    val headPx = lessonState.playBackHeadPosition * dimensions.canvasWidthPx
    val pxPerBeat = dimensions.canvasWidthPx / lessonState.canvasWidthBeats

    for (note in notesInView){

        val percentAccurate = frameRenderState
            .noteAccuracies
            .firstOrNull { it.idx == note.idx }
            ?.percentOnPitch
            ?: 0f

        val distanceFromHeadBeats = note.startsOnBeat - frameRenderState.playBackHeadBeats
        val startX = headPx + (distanceFromHeadBeats * pxPerBeat)
        val noteWidthPx = note.durationRelative * pxPerBeat
        drawNote(
            note,
            percentAccurate,
            dimensions,
            startX,
            noteHeightPx,
            noteWidthPx
        )
    }
}

fun DrawScope.drawNote(
    note: DisplayNote,
    percentAccurate: Float,
    dimensions: LessonCanvasDimensions,
    startX: Float,
    noteHeight: Float,
    noteWidth: Float,
){
    // A note must also graphically serve as a percentage bar, meaning we need a split rounded rectangle
    // conceptually this is a single rounded rectangle with two rectangles inside it

    val baseColour = if (note.isCurrentNote) {Color(0xFFFFF08D) // TODO colours should be dynamic
    } else {Color(0xFF8DC0FF)}

    val completeColour = Color(0xFF95FF8D) // TODO colours should be dynamic

    val centreY = midiToY(note.pitchMidi, dimensions)

    val topLeftOffset = Offset(
        startX,
        centreY - (noteHeight / 2)
    )

    val rectSize = Size(
        noteWidth,
        noteHeight
    )

    val roundedRect = RoundRect(
        rect = Rect(
            offset = topLeftOffset,
            size = rectSize
        ),
        cornerRadius = CornerRadius(
            x = noteHeight / 2f,
            y = noteHeight / 2f
        )
    )

    // controls where the rect will be split to indicate progress
    val splitX = topLeftOffset.x + rectSize.width * percentAccurate

    clipPath(
        Path().apply {
            addRoundRect(roundedRect)
        }
    ){
        drawRect(
            color = completeColour,
            topLeft = topLeftOffset,
            size = Size(
                rectSize.width * percentAccurate,
                rectSize.height
            )
        )

        drawRect(
            color = baseColour,
            topLeft = Offset(splitX,topLeftOffset.y),
            size = Size(
                rectSize.width - (rectSize.width * percentAccurate),
                rectSize.height
            )
        )
    }
}

/**
 * Generate octave dividers for pitch display
 *
 * @param dimensions the dimensions of the canvas
 * @param colour (colour of midi lines on screen)
 */
fun DrawScope.drawOctaveBoundaries(
    dimensions: LessonCanvasDimensions,
    colour: Color = Color(0xFF343A40) // TODO this should not be hardcode
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

/**
 * Generate centre line to mark playback head
 */
fun DrawScope.drawPlaybackHead(
    dimensions: LessonCanvasDimensions,
    colour: Color = Color(0xFF343A40) // TODO should not be hardcoded
){
    val xCoord = dimensions.canvasWidthPx * dimensions.playBackHeadPosition
    drawLine(
        colour,
        Offset(xCoord,dimensions.canvasHeightPx),
        Offset(xCoord,0f)
    )
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


/**
 * Object to store lesson canvas dimensions required by multiple draw functions
 *
 * @param minPitchMidi
 * @param maxPitchMidi
 * @param canvasWidthPx
 * @param canvasHeightPx
 * @param playBackHeadPosition position of playback head as fraction of screen size
 */
data class LessonCanvasDimensions(
    val minPitchMidi: Float,
    val maxPitchMidi: Float,
    val canvasWidthPx: Float,
    val canvasHeightPx: Float,
    val playBackHeadPosition: Float,
    val durationSeconds: Float,
    val pitchCurveDurationSeconds: Float
)