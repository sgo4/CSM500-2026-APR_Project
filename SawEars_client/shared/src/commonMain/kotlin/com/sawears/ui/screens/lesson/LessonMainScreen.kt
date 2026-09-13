package com.sawears.ui.screens.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.model.pitch.PitchResult
import com.sawears.ui.screens.lesson.components.LessonCanvas
import com.sawears.ui.shared.components.ContentWithTopBar
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.lesson_button_pauseaudio_description
import sawears.shared.generated.resources.lesson_button_pauselesson_description
import sawears.shared.generated.resources.lesson_button_playaudio_description
import sawears.shared.generated.resources.lesson_button_playlesson_description
import sawears.shared.generated.resources.lesson_button_restartlesson_description

/**
 * Main lesson screen
 *
 * @param viewModel
 */
@Composable
fun LessonMainScreen(
   viewModel: LessonViewModel,
   onClickMenu: () -> Unit = {},
   onClickAccount: () -> Unit = {},
   onClickSettings: () -> Unit = {}
){

    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()
    // allow user to show/hide top bar to maximise screen space
    var showTopBar by rememberSaveable { mutableStateOf(false)}

    DisposableEffect(viewModel){
        if (!lessonState.recordAudio) {
            viewModel.startPitchPrediction()
        }

        onDispose {
            viewModel.stopPitchPrediction()
        }
    }

    ContentWithTopBar(
        content = { innerPadding -> LessonMainScreenContent(
            viewModel = viewModel,
            lessonState = lessonState,
            onContentTap = { showTopBar = !showTopBar },
            modifier = Modifier.padding(innerPadding)
        )},
        title = lessonState.song.title,
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings,
        showTopBar = showTopBar
    )
}

@Composable
fun LessonMainScreenContent(
    viewModel: LessonViewModel,
    lessonState: LessonUiState,
    onContentTap: () -> Unit = {},
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onContentTap) // clickable to toggle top bar visibility
    ){
        DebugText(lessonState) // TODO remove
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ){
            IconButton(
                onClick = viewModel::toggleLessonPlay
            ){
                Icon(
                    imageVector = if (lessonState.paused){
                        Icons.Filled.PlayArrow
                    } else {
                        Icons.Filled.Pause
                    },
                    contentDescription = if (lessonState.paused)
                        stringResource(Res.string.lesson_button_playlesson_description)
                        else stringResource(Res.string.lesson_button_pauselesson_description)
                )
            }

            IconButton(
                onClick = viewModel::playAudio
            ){
                Icon(
                    imageVector = if (lessonState.audioPaused){
                        Icons.Filled.VolumeUp
                    } else {
                        Icons.Filled.VolumeOff
                    },
                    contentDescription = if (lessonState.audioPaused)
                        stringResource(Res.string.lesson_button_playaudio_description)
                    else stringResource(Res.string.lesson_button_pauseaudio_description)
                )
            }

            IconButton(
                onClick = viewModel::restartLesson
            ){
                Icon(
                    imageVector = Icons.Filled.Replay,
                    contentDescription = stringResource(Res.string.lesson_button_restartlesson_description)
                )
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
private fun DebugText(
    lessonUiState: LessonUiState
){

    var showDebug by rememberSaveable { mutableStateOf(true)}
    Box(
        modifier = Modifier
            .clickable(
                onClick = {showDebug = !showDebug} // tap debug to hide it
            )
    ){
        Text(
        """${lessonUiState.song} || 
            |tempo=${lessonUiState.tempoBpm} ||
            |pitchMargin=${lessonUiState.pitchDisplayMarginMidi}
            |leniencyCents=${lessonUiState.leniencyThresholdCents}""".trimMargin()
        )
    }

}


/**
 * Represents pitch detection state for a single frame#
 *
 * @param pitchSamples
 * @param frameNanos
 * @param offsetNanos offset required to sync with animation timeline
 */
data class PitchDetectionAnimationState(
    val pitchSamples: List<PitchResult> = emptyList(),
    val frameNanos: Long = 0L,
    val offsetNanos: Long = 0L
)

/**
 * Converts pitch samples into observable UI state
 *
 * @param viewModel
 * @param paused
 * @param pitchCurveDurationSeconds
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

            if (!paused){
                val latestPitch = pitchSamples.lastOrNull()
                viewModel.onPlaybackFrame(frameDeltaNanos, latestPitch)
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