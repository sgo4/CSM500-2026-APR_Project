package com.sawears.ui.screens.selectsong

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.tooling.preview.Preview
import com.sawears.data.database.SongEntity
import com.sawears.ui.shared.components.ContentWithTopBar

/**
 * Screen to search and select a song from the database
 *
 * Lazy column approach to database searching partially draws from the tutorial code here:
 * https://github.com/philipplackner/RoomGuideAndroid/
 */
@Composable
fun SongSelectionScreen(
    state: SongSelectionState,
    onSearch: (String) -> Unit,
    onSelectSong: (String) -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    modifier: Modifier = Modifier
){

    val textFieldState = rememberTextFieldState()
    ContentWithTopBar(
        content = { innerPadding ->
            SongSelectionSearchBar(
                textFieldState = textFieldState,
                onSearch = onSearch,
                onSelectItem = onSelectSong,
                searchResults = state.songs,
                modifier = Modifier.padding(innerPadding)
            )
        },
        title = "Song Selection",
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount
    )
}

@Preview
@Composable
fun SongSelectionScreenPreview(){
    val state = SongSelectionState()
    val onSearch = {string: String -> Unit}
    val onSelectSong = {string: String -> Unit}

    ContentWithTopBar(
        content = { innerPadding ->
            SongSelectionSearchBar(
                textFieldState = TextFieldState(),
                onSearch = onSearch,
                onSelectItem = onSelectSong,
                searchResults = listOf(
                    SongEntity(
                        id=1,
                        title="Never Gonne Give You Up",
                        year=1987,
                        description="Never Gonne Let You Down",
                        resourcePath="./"
                    )
                ),
                modifier = Modifier.padding(innerPadding)
            )
        },
        title = "Song Selection",
        onClickMenu = {},
        onClickAccount = {}
    )

}