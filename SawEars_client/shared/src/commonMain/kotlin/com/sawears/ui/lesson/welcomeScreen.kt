package com.sawears.ui.lesson

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LoadingScreen(){
    Text("Loading...")
}

@Composable
fun ErrorScreen(error: String){
    Text("Error: $error")
}
