package com.sawears.ui.screens.error

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sawears.ui.screens.components.ContentWithTopBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.error_page


/**
 * Generic error screen for errors which need to be handled, but don't have
 * any more specific resolution. Shows an error message and allows user
 * to navigate back to home screen.
 */
@Composable
fun GenericErrorScreen(
    errorMsg: String,
    onClickHome: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
) {

    ContentWithTopBar(
        content = { innerPadding ->
            Box(
                modifier=modifier.padding(innerPadding)
            ){
                Text(
                    errorMsg
                )
            }
        },
        title = stringResource(Res.string.error_page),
        onClickHome = onClickHome,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )

}