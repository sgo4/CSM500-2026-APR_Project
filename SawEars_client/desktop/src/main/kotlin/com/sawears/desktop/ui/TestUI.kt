package com.sawears.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.ln
import kotlin.math.ceil
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

fun TestApp() = application{

    val container = DefaultAppContainer()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Real-time pitch graph"
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                mainScreen(container)
            }
        }
    }
}

@Composable
fun mainScreen(container: AppContainer){

    val viewModel = viewModel {LessonViewModel(container)}


    LaunchedEffect(Unit) {
        viewModel.loadSong("somewhereovertherainbow")
    }

    Column(){
        Text("Main Screen")
        when {
//            viewModel.data != null -> Text(viewModel.data!!.title)
            !viewModel.loading ->   lessonCanvas(
                20.0,
                System.nanoTime(),
                viewModel
            )
        }
    }

}

/**
 * Canvas for piano roll style lesson
 *
 * @param minPitchMidi min pitch to display
 * @param maxPitchMidi max pitch to display
 * @param durationSeconds time range to display
 * @param currentTimeNanos current timestamp
 */
@Composable
fun lessonCanvas(
    durationSeconds: Double,
    currentTimeNanos: Long = 1L,
    viewModel: LessonViewModel
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

    Column(modifier = Modifier.fillMaxSize()){
        val player = lessonState.player?: return

        Text(player.song.title)
        Button(
            viewModel::togglePlay
        ){
            if (lessonState.paused){
                Text("Play")
            } else{
                Text("Pause")
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
            val pitchDisplayMarginMidi = 6f
            val minPitchMidi = player.song.songMinMidi - pitchDisplayMarginMidi
            val maxPitchMidi = player.song.songMaxMidi + pitchDisplayMarginMidi

            // canvas height must be >= range of displayable notes otherwise they won't fit on screen
            if (canvasHeightPx < (maxPitchMidi-minPitchMidi)){
                return@Canvas
            }

            drawPianoRoll(
                lessonState,
                minPitchMidi,
                maxPitchMidi,
                canvasWidthPx,
                canvasHeightPx
            )
        }
    }
}



fun DrawScope.drawPianoRoll(
    lessonState: LessonUiState,
    minPitchMidi: Float,
    maxPitchMidi: Float,
    canvasWidthPx: Float,
    canvasHeightPx: Float
){
    val player = lessonState.player?: return
    val notesInView = player.notesInView(lessonState.frameMillis)
    // calculate noteheight as canvas height / range of displayable notes
    val noteHeightPx = canvasHeightPx / (maxPitchMidi - minPitchMidi)
    // calculate startX
    val headPx = player.playBackHeadPosition * canvasWidthPx
    val pxPerBeat = canvasWidthPx / player.canvasWidthBeats

    for (note in notesInView){

        val distanceFromHeadBeats = note.startsOnBeat - player.playBackHeadBeats
        val startX = headPx + (distanceFromHeadBeats * pxPerBeat)
        val noteWidthPx = note.durationRelative * pxPerBeat
        drawNote(
            note,
            minPitchMidi,
            maxPitchMidi,
            startX,
            canvasHeightPx,
            noteHeightPx,
            noteWidthPx
        )
    }
}
fun DrawScope.drawNote(
    note: DisplayNote,
    minMidi: Float,
    maxMidi: Float,
    startX: Float,
    canvasHeight: Float,
    noteHeight: Float,
    noteWidth: Float,
){

    val colour = if (note.isCurrentNote) {Color(0xFFFFF08D)
    } else {Color(0xFF8DC0FF)}
    val centreY = midiToY(note.pitchMidi, minMidi, maxMidi,canvasHeight)

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
 * @param minMidi
 * @param maxMidi
 * @param colour (colour of midi lines on screen)
 * @param canvasWidth
 * @param canvasHeight
 */
fun DrawScope.drawOctaveBoundaries(
    minMidi: Float,
    maxMidi: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    colour: Color = Color(0xFF343A40)
){

    val firstMidiLine = ceil(minMidi / 12f).toInt() * 12

    var midiLine = firstMidiLine

    while(midiLine <= maxMidi){
        val yCoord = midiToY(
            midiLine.toFloat(),
            minMidi,
            maxMidi,
            canvasHeight
        )

        drawLine(
            colour,
            Offset(0f,yCoord),
            Offset(canvasWidth, yCoord),
            strokeWidth = 1f
        )

        midiLine += 12 // note: 12 semitones in an octave
    }
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

fun startBeatToStartX(startBeat: Float, headBeat: Float, headPx: Float, canvasWidth: Float, canvasWidthBeats: Float): Float{

    val distanceFromHeadBeats = startBeat - headBeat
    val pxPerBeat = canvasWidthBeats / canvasWidth

    return headPx * (distanceFromHeadBeats * pxPerBeat)
}