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
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.app_name
import sawears.shared.generated.resources.home_page_button_startlesson
import sawears.shared.generated.resources.home_page_error
import sawears.shared.generated.resources.home_page_loading


@Composable
fun MainScreen(
    onOpenLesson: () -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){
    ContentWithTopBar(
        content = {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = onOpenLesson
                ) {
                    Text(
                        stringResource(Res.string.home_page_button_startlesson)
                    )
                }
            }
        },
        title = stringResource(Res.string.app_name),
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )


}
@Composable
fun LoadingScreen(){
    Text(stringResource(Res.string.home_page_loading))
}

@Composable
fun ErrorScreen(error: String){
    Text(stringResource(Res.string.home_page_error, error))
}
