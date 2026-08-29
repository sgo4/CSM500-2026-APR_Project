package com.sawears.ui.screens.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.model.pitch.PitchResult
import com.sawears.ui.screens.lesson.components.LessonCanvas
import com.sawears.ui.screens.lessonsettings.LessonSettings

/**
 * Main lesson screen
 *
 * @param viewModel
 * @param lessonId
 */
@Composable
fun LessonMainScreen(
   viewModel: LessonViewModel,
   lessonId: Int
){

    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()
    val settings by viewModel.lessonSettings.collectAsStateWithLifecycle()

    // TODO evaluate where in the flow to actually start recording
    DisposableEffect(viewModel){
        if (!lessonState.recordAudio) {
            viewModel.startPitchPrediction()
        }

        onDispose {
            viewModel.stopPitchPrediction()
        }
    }

    Column(modifier = Modifier.fillMaxSize()){
        debugText(settings)

        Row(modifier = Modifier.fillMaxWidth()){
            Button(
                viewModel::toggleLessonPlay
            ){
                if (lessonState.paused){
                        Text("Play")
                    } else{
                        Text("Pause")
                    }
            }

            Button(
                viewModel::playAudio
            ){
                if (lessonState.audioPaused){
                        Text("Play Audio")
                    } else{
                        Text("Pause Audio")
                    }
            }

        }

        val pitchDetectionAnimationState = providePitchDetectionstate(
            viewModel,
            lessonState.paused,
            pitchCurveDurationSeconds = ( // calculate length of pitch history (i.e. proportion of time window before playback head)
                    lessonState.durationSeconds * lessonState.playBackHeadPosition
                    )
        )

        LessonCanvas(
            lessonState,
            pitchDetectionAnimationState
        )
    }
}

/*
Debug text // TODO remove from final version
 */
@Composable
private fun debugText(lessonSettings: LessonSettings){
    Text(
        "${lessonSettings.song} || tempo=${lessonSettings.tempo}"
    )
}


/**
 * Represents pitch detection state for a single frame
 */

data class PitchDetectionAnimationState(
    val pitchSamples: List<PitchResult> = emptyList(),
    val frameNanos: Long = 0L,
    val offsetNanos: Long = 0L
)

/**
 * Respo
 */
@Composable
fun providePitchDetectionstate(
    viewModel: LessonViewModel,
    paused: Boolean,
    pitchCurveDurationSeconds: Float
): State<PitchDetectionAnimationState>{

    val currentWindow by rememberUpdatedState(pitchCurveDurationSeconds)

    return produceState(
        initialValue = PitchDetectionAnimationState(),
        viewModel,
        paused
    ){

        val pitchSamples = ArrayDeque<PitchResult>()
        var previousFrameNanos = withFrameNanos { it }

        while (true){
            val currentFrameNanos = withFrameNanos { it }
            val frameDeltaNanos = (currentFrameNanos - previousFrameNanos)
            previousFrameNanos = currentFrameNanos

            if (!paused){
                viewModel.onPlaybackFrame(frameDeltaNanos)
            }

            // add latest pitch samples
            viewModel.pitchCurveInput.drainInto(pitchSamples)

            val currentSystemTime = System.nanoTime()

            // calculate an offset between youngest sample and current time,
            // in order to treat youngest sample as age=0
            val pitchOffset = pitchSamples.lastOrNull()
                ?.let { (currentSystemTime - it.timeStampNanos).toLong()}
                ?: 0L
            val oldestAllowed =
                System.nanoTime() - (currentWindow * 1_000_000_000).toLong() - pitchOffset

            while (
                pitchSamples.isNotEmpty() &&
                pitchSamples.first().timeStampNanos < oldestAllowed

            ) {
                pitchSamples.removeFirst()
            }

            // emit animation state that can be consumed by drawables
            value = PitchDetectionAnimationState(
                pitchSamples = pitchSamples.toList(),
                frameNanos = currentSystemTime,
                offsetNanos = pitchOffset
            )
        }
    }
}