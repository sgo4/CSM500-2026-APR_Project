package com.sawears

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sawears.ui.lesson.lessonMainScreen

fun SawEarsApp() = application {

    val container = WindowsAppContainer()

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