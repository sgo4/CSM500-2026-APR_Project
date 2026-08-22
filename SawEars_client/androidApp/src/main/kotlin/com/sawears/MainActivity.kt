package com.sawears

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sawears.data.database.seedDatabase
import com.sawears.ui.lesson.ErrorScreen
import com.sawears.ui.lesson.LoadingScreen
import com.sawears.ui.screens.permissions.PermissionsRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {


        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // TODO replace with view model?
            var initialized by remember { mutableStateOf(false) }
            var initializationError by remember {mutableStateOf("")}

            LaunchedEffect(Unit){
                try {
                    seedDatabase(container.db)
                    initialized = true
                } catch (error: Throwable){
                    initializationError = error.toString()
                }
            }

            when {
                initializationError.isNotEmpty() ->
                    ErrorScreen(initializationError)
                // once db initalized, proceed to permissions flow
                initialized -> PermissionsRoute { App(container) }
                else -> LoadingScreen()
            }
        }
    }
}

