package com.sawears.ui.screens.selectsong

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text

/**
 * Screen to search and select a song from the database
 *
 * Lazy column approach to database searching partially draws from the tutorial code here:
 * https://github.com/philipplackner/RoomGuideAndroid/
 */
@Composable
fun SongSelectionScreen(
    state: SongSelectionState,
    onSelectSong: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxSize()
){

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        items(state.songs){ song ->
            Row(
                modifier = modifier.fillMaxWidth()
            ){
                Column(
                    modifier = Modifier.weight(1f)
                ){
                    Text(
                        text = "${song.title} ${song.year}", // TODO work out how to stitch composers back in
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSong(song.resourcePath)
                            }
                    )
                }
            }
        }
    }
}