package com.sawears.data.midijson.DTOs

import kotlinx.serialization.Serializable

/**
 * Final data layer representation of a song parsed from midiJSON
 *
 * @property title (required)
 * @property composers (required)
 * @property year year of release (optional)
 * @property description (optional)
 * @property tempo a recommended tempo (optional)
 * @property notes as midi values and relative durations (required)
 */
@Serializable
data class MidiJsonSongDto(
    val title: String,
    val composers: List<String>,
    val year: Int?,
    val tempo: Int?,
    val description: String?,
    val notes: List<MidiJsonSongNoteDto>
)

/**
 * Extension function to convert MidiJsonFileDto to MidiJsonSongDto
 */
internal fun MidiJsonFileDto.toMidiJsonSongDto(): MidiJsonSongDto {

    return MidiJsonSongDto(
        title,
        composers,
        year,
        tempo,
        description,
        notes.map {
            it.toMidiJsonSongNoteDto()
        }
    )
}