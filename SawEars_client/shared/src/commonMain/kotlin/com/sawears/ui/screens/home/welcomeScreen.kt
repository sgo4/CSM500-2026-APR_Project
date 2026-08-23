package com.sawears.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sawears.ui.shared.components.ContentWithTopBar


@Composable
fun MainScreen(
    onOpenLesson: () -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    modifier: Modifier = Modifier
){
    ContentWithTopBar(
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = onOpenLesson
                ) {
                    Text("Start lesson")
                }
            }
        },
        title = "SawEars - Home",
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount
    )


}
@Composable
fun LoadingScreen(){
    Text("Loading...")
}

@Composable
fun ErrorScreen(error: String){
    Text("Error: $error")
}
