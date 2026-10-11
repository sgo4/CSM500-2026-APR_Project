package com.sawears

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sawears.data.database.seedDatabase
import com.sawears.ui.navigation.NavigationRoot
import com.sawears.ui.theme.SawEarsTheme
import kotlinx.coroutines.runBlocking

/**
 * SawEars main desktop application
 *
 * @param container [AppContainer] containing dependenices
 */
fun SawEarsApp(container: AppContainer) = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "SawEars"
    ) {
        SawEarsTheme {
            Surface(Modifier.fillMaxSize()) {
                NavigationRoot(container)
            }
        }
    }
}

/**
 * Main application entry point. Creates dependencies and starts application.
 *
 * Design notes:
 * - Since most features rely on a working database, database construction occurs before app is opened
 */
fun main() = runBlocking {

    // initialize app container
    val container = WindowsAppContainer()
    // seed database (if needed)
    seedDatabase(container.db)
    // launch app
    SawEarsApp(container)

}