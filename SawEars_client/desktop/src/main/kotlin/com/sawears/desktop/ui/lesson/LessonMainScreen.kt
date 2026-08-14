package com.sawears.desktop.ui.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sawears.desktop.AppContainer
import com.sawears.desktop.ui.lesson.components.lessonCanvas

/**
 * Main lesson screen
 */
@Composable
fun lessonMainScreen(container: AppContainer){

    val viewModel = viewModel { LessonViewModel(container) }
    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()
    if (!lessonState.recordAudio) {
        viewModel.startPitchPrediction()
    }
    LaunchedEffect(Unit) {
        if (lessonState.song == null) {
            viewModel.loadSong("somewhereovertherainbow")
        }
    }
    println(lessonState.recordAudio)

    Column(modifier = Modifier.fillMaxSize()){
        Text("Main Screen")
        Text(lessonState.player?.song?.title?: "")

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
            lessonCanvas(
                viewModel,
                lessonState,
                20f,
                5f
            )}
        }
    }
}