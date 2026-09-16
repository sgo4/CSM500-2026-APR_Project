package com.sawears.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sawears.ui.screens.components.ContentWithTopBar
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.account_page_buttons_home
import sawears.shared.generated.resources.account_page_placeholdertext

/**
 * Placeholder account management screen.
 * Account features are not planned for initial realse, so this
 * screen simply shows placeholder text.
 */
@Composable
fun AccountScreen(
    onClickHome: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    onBack: () -> Unit
){

    ContentWithTopBar(
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ){

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(stringResource(Res.string.account_page_placeholdertext))
                    Button(
                        onClick = onBack
                    ){
                        Text(stringResource(Res.string.account_page_buttons_home))
                    }
                }
            }
        },
        title = "Account",
        onClickMenu = onClickHome,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )
}

@Preview
@Composable
fun AccountScreenPreview(){
    AccountScreen({},{},{}, {})
}