package com.sawears.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sawears.desktop.DefaultAppContainer
import com.sawears.desktop.ui.lesson.lessonMainScreen


fun SawEarsApp() = application {

    val container = DefaultAppContainer()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Real-time pitch graph"
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                lessonMainScreen(container)
            }
        }
    }
}

fun main(){
    println("starting")
    SawEarsApp()
}