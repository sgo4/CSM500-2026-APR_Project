package com.sawears.ui.screens.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.model.Song
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()

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

        LessonCanvas(
            viewModel,
            lessonState,
            20f,
            5f
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