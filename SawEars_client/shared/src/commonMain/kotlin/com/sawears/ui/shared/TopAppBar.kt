package com.sawears.ui.shared

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

/**
 * Top app bar component for use on most screens
 *
 * Adapted from boilerplate code found at:
 * https://developer.android.com/develop/ui/compose/components/app-bars
 */
@Composable
fun ContentWithTopBar(
    content: @Composable (PaddingValues) -> Unit,
    title: String,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClickMenu) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onClickAccount) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Account"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = content
    )
}




@Composable
fun ExampleContent(){
    Text("Screen Content goes here")
}

@Preview
@Composable
fun ContentWithTopBarPreview(){
    ContentWithTopBar(
        content = {ExampleContent()},
        title = "Sawears\nMain Menu",
        onClickAccount = {},
        onClickMenu = {}
    )
}



