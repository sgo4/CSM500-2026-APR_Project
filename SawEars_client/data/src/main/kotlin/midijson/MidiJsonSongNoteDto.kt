package com.sawears.data.midijson

import kotlinx.serialization.Serializable

/**
 * Final data layer representation of a single note
 *
 * At this layer, notes are still represented as midivalues
 * and durations are represented as fractions of a single beat
 * @property pitchMidi as midi value
 * @property durationRelative relative to crotchet
 */
@Serializable
data class MidiJsonSongNoteDto(
    val pitchMidi: Int,
    val durationRelative: Double
)

/**
 * Extension function to convert a MidiJsonFileNoteDto
 * to a MidiSongNoteDto
 */
internal fun MidiJsonFileNoteDto.toMidiJsonSongNoteDto(): MidiJsonSongNoteDto{
    return MidiJsonSongNoteDto(
        pitch,
        duration / 3
    )
}