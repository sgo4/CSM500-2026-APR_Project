package com.sawears.desktop.ui.lesson

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sawears.desktop.AppContainer
import com.sawears.desktop.ui.lesson.components.lessonCanvas

/**
 * Main lesson screen
 */
@Composable
fun lessonMainScreen(container: AppContainer){

    val viewModel = viewModel { LessonViewModel(container) }

    LaunchedEffect(Unit) {
        viewModel.loadSong("somewhereovertherainbow")
    }

    Column(){
        Text("Main Screen")
        when {
            !viewModel.loading ->   lessonCanvas(
                20.0,
                System.nanoTime(),
                viewModel
            )
        }
    }
}