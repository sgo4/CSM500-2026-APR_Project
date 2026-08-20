package com.sawears

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sawears.data.database.createDatabase
import com.sawears.data.database.getDatabaseBuilder
import com.sawears.ui.lesson.LessonMainScreen
import kotlinx.coroutines.runBlocking

/**
 * SawEars main desktop application
 *
 * @param contaier app container containing dependenices
 */
fun SawEarsApp(container: AppContainer) = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "SawEars"
    ) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                LessonMainScreen(container)
            }
        }
    }
}

/**
 * Main application entry point. Creates dependencies and starts application.
 *
 * //TODO use a proper DI framework
 */
fun main() = runBlocking {
    println("starting")
    val container = WindowsAppContainer.create()
    SawEarsApp(container)
}