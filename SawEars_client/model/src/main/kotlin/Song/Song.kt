package com.sawears.model.Song

import com.sawears.data.midijson.MidiJsonSongDto


/**
 * Domain layer representation of a song
 *
 * Agnostic to final tempo at this stage
 * @property title (required)
 * @property composers (required)
 * @property year year of release (optional)
 * @property description (optional)
 * @property tempo a recommended tempo (optional)
 * @property notes a series of pitches and durations (required)
 */
data class Song(
    val title: String,
    val composers: List<String>,
    val year: Int?,
    val tempo: Int?,
    val description: String?,
    val notes: List<Note>
) {

    /**
     * Return a transposed Song object by a defined number of semitones
     */
    fun transpose(semitones: Int): Song {
        val transposedNotes = notes.map {
            it -> it.transpose(semitones)
        }
        return Song(
            title,
            composers,
            year,
            tempo,
            description,
            transposedNotes
        )
    }
}

/**
 * Extension function to convet data layer song representation to domain layer
 */
fun MidiJsonSongDto.toSong(): Song{
    return Song(
        title,
        composers,
        year,
        tempo,
        description,
        notes.map {
            it.toNote()
        }
    )
}