package com.sawears.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sawears.AppContainer
import java.awt.Button


@Composable
fun MainScreen(
    onOpenLesson: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Main screen")
        Button(
            onClick = onOpenLesson
        ) {
            Text("Start lesson")
        }
    }

}
@Composable
fun LoadingScreen(){
    Text("Loading...")
}

@Composable
fun ErrorScreen(error: String){
    Text("Error: $error")
}
