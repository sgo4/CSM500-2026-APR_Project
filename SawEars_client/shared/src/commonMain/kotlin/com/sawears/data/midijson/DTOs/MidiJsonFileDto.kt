package com.sawears.data.midijson.DTOs

import kotlinx.serialization.Serializable

/**
 * Dto representing a parsed midiJSON file
 *
 * @property title (required)
 * @property composers (required)
 * @property year year of release (optional)
 * @property description (optional)
 * @property tempo a recommended tempo (optional)
 * @property notes as midi pitch and relative duration (required)
 */
@Serializable
internal data class MidiJsonFileDto(
    val title: String,
    val composers: List<String>,
    val year: Int? = null,
    val tempo: Int? = null,
    val description: String? = null,
    val notes: List<MidiJsonFileNoteDto>
)