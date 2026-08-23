package com.sawears.ui.screens.selectsong

import com.sawears.data.database.SongEntity

/**
 * State holder for song selection screen
 */
data class SongSelectionState(
    val songs: List<SongEntity> = emptyList()
)