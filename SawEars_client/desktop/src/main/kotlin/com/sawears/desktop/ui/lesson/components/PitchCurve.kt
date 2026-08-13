package com.sawears.desktop.ui.lesson.components


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import com.sawears.model.pitch.PitchResult
import java.util.concurrent.ConcurrentLinkedQueue

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke


fun DrawScope.drawPitchCurve(
    samples: List<PitchResult>,
    dimensions: LessonCanvasDimensions,
    frameNanos: Long,
    colour: Color = Color(0xFF405061)
){

    val path = Path()
    var segmentStarted = false

    for (sample in samples){

        // ignore samples older than display range
        val ageSeconds = (System.nanoTime() - sample.timeStampNanos).toFloat() / 1_000_000_000f

        if (ageSeconds !in 0f..dimensions.durationSeconds){
            continue
        }
        // break path if prediction invalid
        if (sample.frequencyHz == null){
            segmentStarted = false
            continue
        }

        val pitchMidi = hzToMidi(sample.frequencyHz!!.toFloat())

        // break path if pitch outside displayable range
        if (pitchMidi !in dimensions.minPitchMidi .. dimensions.maxPitchMidi){
            segmentStarted = false
            continue
        }

        // calculate coordinates
        val xCoord = dimensions.canvasWidthPx *
                (1 - ageSeconds / dimensions.durationSeconds) * dimensions.playBackHeadPosition
        val yCoord = midiToY(pitchMidi, dimensions)

        if (!segmentStarted) {
            path.moveTo(xCoord,yCoord)
            segmentStarted = true
        } else {
            path.lineTo(xCoord, yCoord)
        }
    }

	drawPath(
        path=path,
        color=colour,
        style = Stroke(
            width = 3f,
            cap = StrokeCap.Round
        )
    )
}

/**
 * Takes input from pitch model and feeds into display
 */
class PitchCurveInput {

    private val pendingSamples = ConcurrentLinkedQueue<PitchResult>()

    /**
     * Take pitch from a pitch model
     *
     * @param pitchResult
     */
    fun submitPitch(
        pitchResult: PitchResult
    ){
        val validatedPitch = pitchResult.frequencyHz?.takeIf {
            it.isFinite() && it > 0f
        }

        pendingSamples.add(
            PitchResult(
                validatedPitch,
                pitchResult.timeStampNanos
            )
        )
    }

    /**
     * Drain collected samples into a receiver (e.g. a composable)
     */
    fun drainInto(destination: MutableList<PitchResult>) {
        while (true) {
            val sample = pendingSamples.poll() ?: break
            destination.add(sample)
        }
    }
}
