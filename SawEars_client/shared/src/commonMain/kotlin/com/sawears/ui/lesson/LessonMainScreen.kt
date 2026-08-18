package com.sawears.ui.lesson

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sawears.AppContainer
import com.sawears.ui.lesson.components.LessonCanvas

/**
 * Main lesson screen
 */
@Composable
fun LessonMainScreen(container: AppContainer){

    val viewModel = viewModel { LessonViewModel(container) }
    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()

    // TODO evaluate where in the flow to actually start recording
    DisposableEffect(viewModel){
        if (!lessonState.recordAudio) {
            viewModel.startPitchPrediction()
        }

        onDispose {
            viewModel.stopPitchPrediction()
        }
    }

    LaunchedEffect(Unit) {
        if (lessonState.song == null) {
            val path = viewModel.getResourcePath("Somewhere Over the Rainbow")
            println(path)
            viewModel.loadSong(path!!) // TODO add a real search select feature
        }
    }

    Column(modifier = Modifier.fillMaxSize()){
        Text("Main Screen")
        Text(lessonState.player?.song?.title?: "Loading...")

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

    when {
        !viewModel.loading ->   {
            LessonCanvas(
                viewModel,
                lessonState,
                20f,
                5f
            )}
        }
    }
}