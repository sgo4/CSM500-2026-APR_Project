package com.sawears.data.midijson

import kotlinx.serialization.Serializable

/**
 * Represents a single note in a melody directly parsed from midiJSon
 *
 * @property pitch as midi value
 * @property duration the duration of the note (relative to crotchet)
 */
@Serializable
internal data class MidiJsonFileNoteDto(
    val pitch: Float,
    val duration: Double
)