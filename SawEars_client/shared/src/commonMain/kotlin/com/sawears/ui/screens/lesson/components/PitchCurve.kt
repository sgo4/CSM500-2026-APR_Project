package com.sawears.ui.screens.lesson.components


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import com.sawears.model.pitch.PitchResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.sawears.diagnostics.RealTimeLatencyTracker

/**
 * Draw a curve representing current pitch + recent pitch history.
 * Due to the need to always show some value at the playback head, an offset is used to calculate
 * ages relative to the youngest sample.
 *
 * @param samples a stream of pitch samples
 * @param dimensions information relating to the canvas dimensions
 * @param systemTime represents the current time (fixed at the time at which offset was calculated for simplicity)
 * @param timeOffset the difference between the timestamp of the youngest sample and system time
 * @param colour the colour of the pitch curve on screen
 */
fun DrawScope.drawPitchCurve(
    samples: List<PitchResult>,
    dimensions: LessonCanvasDimensions,
    systemTime: Long,
    timeOffset: Long,
    colour: Color
){

    val path = Path()
    var segmentStarted = false

    for (sample in samples){

        // ignore samples older than display range
        val ageSeconds = (systemTime - (sample.timeStampNanos+timeOffset)).toFloat() / 1_000_000_000f

        if (ageSeconds !in 0f..dimensions.pitchCurveDurationSeconds){
            continue
        }
        // break path if prediction invalid
        if (sample.frequencyHz == null){
            segmentStarted = false
            continue
        }

        val pitchMidi = hzToMidi(sample.frequencyHz.toFloat())

        // break path if pitch outside displayable range
        if (pitchMidi !in dimensions.minPitchMidi .. dimensions.maxPitchMidi){
            segmentStarted = false
            continue
        }

        // calculate coordinates
        val xCoord = dimensions.canvasWidthPx *
                (1 - ageSeconds / dimensions.pitchCurveDurationSeconds) * dimensions.playBackHeadPosition
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
 *
 * @param capacity the number of samples to remember (prevents input growing arbitrarily large)
 */
class PitchCurveInput(
    capacity: Int = 512
) {

    private val pendingSamples = Channel<PitchResult>(
        capacity = capacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

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

        pendingSamples.trySend(
            PitchResult(
                pitchResult.frameId,
                validatedPitch,
                pitchResult.timeStampNanos
            )
        )
    }

    /**
     * Drain collected samples into a receiver
     *
     * @param receiver
     */
    fun drainInto(receiver: MutableList<PitchResult>) {
        val drained = buildList {
            while (true) {
                val result = pendingSamples.tryReceive()

                if (result.isFailure){
                    break
                }
                add(result.getOrThrow())
            }
        }
        receiver.addAll(drained)
    }

    fun close() {
        pendingSamples.close()
    }
}
