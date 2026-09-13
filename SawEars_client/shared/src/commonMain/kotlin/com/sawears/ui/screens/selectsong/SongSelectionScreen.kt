package com.sawears.ui.screens.selectsong

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.tooling.preview.Preview
import com.sawears.data.database.SongEntity
import com.sawears.ui.shared.components.ContentWithTopBar
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.songselection_page_title

/**
 * Screen to search and select a song from the database
 *
 * Lazy column approach to database searching partially draws from the tutorial code here:
 * https://github.com/philipplackner/RoomGuideAndroid/
 *
 * @param state
 * @param onSearch callback function for search
 * @param onSelectSong callback function when a song is selected
 * @param onClickMenu callback for menu button
 * @param onClickAccount callback for account button
 * @param onClickSettings callback for settings button
 */
@Composable
fun SongSelectionScreen(
    state: SongSelectionState,
    onSearch: (String) -> Unit,
    onSelectSong: (Int, String) -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){

    val textFieldState = rememberTextFieldState()
    ContentWithTopBar(
        content = { innerPadding ->
            SongSelectionSearchBar(
                textFieldState = textFieldState,
                onSearch = onSearch,
                onSelectSong = onSelectSong,
                searchResults = state.songs,
                modifier = modifier.padding(innerPadding)
            )
        },
        title = stringResource(Res.string.songselection_page_title),
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )
}

@Preview
@Composable
fun SongSelectionScreenPreview(){
    val state = SongSelectionState()
    val onSearch = {string: String -> Unit}
    val onSelectSong = {lessonId: Int, resourcePath: String -> Unit}

    ContentWithTopBar(
        content = { innerPadding ->
            SongSelectionSearchBar(
                textFieldState = TextFieldState(),
                onSearch = onSearch,
                onSelectSong = onSelectSong,
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
        title = stringResource(Res.string.songselection_page_title),
        onClickMenu = {},
        onClickAccount = {},
        onClickSettings = {}
    )

}