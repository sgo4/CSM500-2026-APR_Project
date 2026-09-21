package com.sawears.ui.screens.selectsong


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawears.data.SongRepository
import com.sawears.data.database.SongDao
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.useCases.RegisterLesson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.songselection_errors_failed_to_load

/**
 * View model for song selection screen
 *
 * Implementation notes:
 * - Song selection owns process of creating lesson as an object known by the lesson settings
 * repository
 * - Search is always fuzzy and in alphabetical order
 * - Blank search field shows all results
 *
 * Sources consulted:
 * - Partly adapted from source code by Philip Lackner found at:
 *      https://github.com/philipplackner/RoomGuideAndroid/
 */
class SongSelectionViewModel(
    private val dao: SongDao,
    private val songRepository: SongRepository,
    private val lessonSettingsRepository: LessonSettingsRepository,
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

    init {
        _searchQuery.value = "" // send blank query on initialization
    }

    /**
     * Fuzzy search by title (e.g. "ever" will match both "Never Gonna Give You Up" and "Together Forever")
     */
    fun searchTitleFuzzy(title: String){
        _searchQuery.value = title
    }

    /**
     * Handles the business logic for when a user selects a song on the song selection screen.
     *
     * 1. Loads song into memory
     * 2. Registers song with lesson settings repository
     * 3. Sends event indicating outcome
     *
     * @param songId
     * @param resourcePath path to resource
     */
    fun selectSong(
        songId: Int,
        resourcePath: String
    ){
        viewModelScope.launch {
            val song = songRepository.load(resourcePath)

            if (song == null){
                _events.send(SongSelectionEvent.SelectionError(
                        message=Res.string.songselection_errors_failed_to_load
                    )
                )
                return@launch
            }

            val globalSettings = globalSettingsRepository.settings.first()

            /*
            Register lesson with lesson settings repository (this allows a specific lesson to be mapped
            to corresponding details such as settings)
             */
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

/**
 * Events broadcast to inform wider program of outcome of suspend operations
 */
sealed interface SongSelectionEvent {
    /**
     * Event indicating song was successfully selected, and user
     * progressed to Lesson Settings screen
     *
     * @param lessonId
     */
    data class OpenLessonSettings(
        val lessonId: Int
    ) : SongSelectionEvent

    /**
     * Event indicatng some error occured during song selection
     *
     * @param message
     */
    data class SelectionError(
        val message: StringResource
    ) : SongSelectionEvent
}