package com.sawears.ui.screens.selectsong


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.database.SongDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val _searchQuery = MutableStateFlow("")
    private val _state = MutableStateFlow(SongSelectionState())
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _results = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dao.getAll()
            } else {
                dao.searchSongsByTitleFuzzy(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    /**
     * Fuzzy search by title (e.g. "ever" will match both "Never Gonna Give You Up" and "Together Forever")
     */
    fun searchTitleFuzzy(title: String){
        _searchQuery.value = title
    }
}