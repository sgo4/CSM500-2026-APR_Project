package com.sawears.desktop.ui

import androidx.lifecycle.ViewModel
import com.sawears.data.MidiJsonRepository
import com.sawears.model.useCases.LoadSong
import com.sawears.model.Note
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sawears.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface AppContainer {
    val loadSong: LoadSong
}

class DefaultAppContainer : AppContainer {
    override val loadSong: LoadSong = LoadSong(
        MidiJsonRepository()
    )
}

/**
 * Experimental working class for a view model holding lesson state
 *
 * Song
 * Time
 * Tempo
 * Current pitch
 */
class LessonViewModel(
    private val container: AppContainer
): ViewModel() {
    val loadSong = container.loadSong

    private val _state = MutableStateFlow(LessonUiState())
    val lessonState = _state.asStateFlow()
    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadSong(title: String) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                val song = loadSong.load(title)!!
                _state.update {
                    LessonUiState(
                        LessonPlayer(song)
                    )
                }
            } catch (exception: Exception) {
                error = exception.message ?: "Failed to load data"
            } finally {
                loading = false
            }
        }
    }

    fun updateClock(frameDeltaMillis: Long){
        _state.update {
            it.copy(
                frameMillis = it.frameMillis + frameDeltaMillis
            )
        }
    }


    fun togglePlay(){
        _state.update {
            it.copy(
                paused = !it.paused
            )
        }
    }
}
data class DisplayNote(
    val pitchMidi: Float,
    val durationRelative: Float,
    val startsOnBeat: Float,
    val isCurrentNote: Boolean
)

data class LessonUiState(
    val player: LessonPlayer? = null,
    val paused: Boolean = true,
    var frameMillis: Long = 0L
)

/**
 * ViewModel representation of a song, keeping track of playback head, elapses time
 */
class LessonPlayer(
    val song: Song,
    defaultTempo: Int = 90,
    val canvasWidthBeats: Float = 5f,
    val playBackHeadPosition: Float = 0.5f
){
    var playBackHeadBeats: Float = 0f
    var systemTimeMillis: Long? = null
    var noteTransitions = FloatArray(song.notes.size)
    var currentNoteIdx = 0
    var currentWindowStartBeats = 0f
    var currentWindowEndBeats = currentWindowStartBeats + canvasWidthBeats
    var latestConsumedNoteIdx = -1
    var consumedNotesBeats = 0f
    var tempo = song.tempo?: defaultTempo
    val pastBeats = canvasWidthBeats * playBackHeadPosition
    val futureBeats = canvasWidthBeats * (1-playBackHeadPosition)


    init {
        // populate noteTransitions with the end of each note
        var boundary = 0f
        for (i in noteTransitions.indices){
            boundary += song.notes[i].durationRelative.toFloat()
            noteTransitions[i] = boundary
        }
    }

    /*
     * Update the position of the playback head in beats elapsed based on time elapses
     *
     * @param currentTime
     */
    private fun updatePlayBackHead(currentTime: Long){
        if (systemTimeMillis == null){
            systemTimeMillis = currentTime
        }
        val elapsedTimeMs = currentTime - systemTimeMillis!!
        val elapsedTimeBeats = elapsedTimeMs / (60_000f / tempo)
        playBackHeadBeats += elapsedTimeBeats
        systemTimeMillis = currentTime
    }

    /**
     * Calculate which notes are in view (fully or partially) at current time
     *
     * @param currentTime
     */
    fun notesInView(currentTime: Long): List<DisplayNote>{

        updatePlayBackHead(currentTime) // move playback head appropriate distance for time elapsed

        while (currentNoteIdx < noteTransitions.size && playBackHeadBeats >= noteTransitions[currentNoteIdx]){
            currentNoteIdx ++ // update current note idx
        }
        // calculate window start / end in terms of cummulative beats elapsed
        currentWindowStartBeats = playBackHeadBeats - pastBeats
        currentWindowEndBeats = playBackHeadBeats + futureBeats

        var windowStartIdx = latestConsumedNoteIdx
        var cummulativeBeats = maxOf(consumedNotesBeats,0f)
        while (windowStartIdx < song.notes.size-1 && cummulativeBeats <= currentWindowStartBeats){
            windowStartIdx++
            cummulativeBeats += song.notes[windowStartIdx].durationRelative.toFloat()
        }
        windowStartIdx = maxOf(windowStartIdx,0)
        latestConsumedNoteIdx = windowStartIdx - 1
        consumedNotesBeats = maxOf(cummulativeBeats - song.notes[windowStartIdx].durationRelative.toFloat(), 0f)

        val res = mutableListOf<DisplayNote>()
        var noteIdx = windowStartIdx
        cummulativeBeats = consumedNotesBeats
        while (noteIdx < song.notes.size-1 && cummulativeBeats <= currentWindowEndBeats){
            val note = song.notes[noteIdx]
            val durationRelative = note.durationRelative.toFloat()
            res.add(
                DisplayNote(
                    note.pitchMidi.toFloat(),
                    durationRelative,
                    cummulativeBeats,
                    isCurrentNote = noteIdx == currentNoteIdx
                )
            )
            cummulativeBeats += durationRelative
            noteIdx++
        }
        return res.toList()

    }
}


