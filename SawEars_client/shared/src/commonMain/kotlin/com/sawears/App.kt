package com.sawears

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sawears.ui.lesson.LessonMainScreen
import com.sawears.ui.lesson.components.LessonCanvas
import org.jetbrains.compose.resources.painterResource

import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.compose_multiplatform
import com.sawears.ui.navigation.NavigationRoot

@Composable
fun App(container: AppContainer) {
    MaterialTheme {
        Scaffold {
            innerPadding ->
            NavigationRoot(
                container,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}