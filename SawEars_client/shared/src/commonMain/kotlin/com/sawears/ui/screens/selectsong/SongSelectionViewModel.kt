package com.sawears.ui.screens.selectsong


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.database.SongDao
import kotlinx.coroutines.flow.*

/**
 * View model for song selection screen
 *
 * Partly adapted from source code by Philip Lackner:
 *  https://github.com/philipplackner/RoomGuideAndroid/
 */
class SongSelectionViewModel(
    private val dao: SongDao
): ViewModel() {

    private val _state = MutableStateFlow(SongSelectionState())
    private val _results = dao
        .getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
    val state: StateFlow<SongSelectionState> = combine(_state, _results){ state, results ->
        state.copy(
            songs = results
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SongSelectionState()
    )

}