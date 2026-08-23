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
import com.sawears.ui.shared.ContentWithTopBar

/**
 * Placeholder account management screen.
 * Account features are not planned for initial realse, but they are
 */
@Composable
fun AccountScreen(
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
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
                    Text("""
                Coming soon!
                Account management features aren't implemented yet.
                In the meantime, all features are usable without creating an account.
            """.trimIndent()
                    )
                    Button(
                        onClick = onBack
                    ){
                        Text("Back home")
                    }
                }
            }
        },
        title = "Account",
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount
    )
}

@Preview
@Composable
fun AccountScreenPreview(){
    AccountScreen({},{},{})
}