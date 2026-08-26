package com.sawears.ui.screens.selectsong


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.SongRepository
import com.sawears.data.database.SongDao
import com.sawears.data.settings.LocalSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.Song
import com.sawears.model.useCases.RegisterLesson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * View model for song selection screen
 *
 * Partly adapted from source code by Philip Lackner:
 *  https://github.com/philipplackner/RoomGuideAndroid/
 */
class SongSelectionViewModel(
    private val dao: SongDao,
    private val songRepository: SongRepository,
    private val lessonSettingsRepository: LocalSettingsRepository,
    private val globalSettingsRepository: SettingsRepository

): ViewModel() {

    private val _events = Channel<SongSelectionEvent>()
    val events = _events.receiveAsFlow()

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

    fun selectSong(songId: Int, resourcePath: String, defaultTempo: Int = 80){

        viewModelScope.launch {
            val song = songRepository.load(resourcePath)

            if (song == null){
                _events.send(SongSelectionEvent.ShowError("Could not load song with id $songId from path $resourcePath"))
                return@launch
            }

            val globalSettings = globalSettingsRepository.settings.first()

            RegisterLesson(
                globalSettings,
                lessonSettingsRepository
            ).register(song,songId)

            _events.send(
                SongSelectionEvent.OpenLessonSettings(songId)
            )
        }



    }
}

sealed interface SongSelectionEvent {
    data class OpenLessonSettings(
        val lessonId: Int
    ) : SongSelectionEvent

    data class ShowError(
        val message: String
    ) : SongSelectionEvent
}