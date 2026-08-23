package com.sawears

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.sawears.ui.navigation.NavigationRoot
import com.sawears.ui.theme.SawEarsTheme

@Composable
fun App(container: AppContainer) {
    SawEarsTheme {
        Scaffold {
            innerPadding ->
            NavigationRoot(
                container,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}