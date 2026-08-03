package com.sawears.model.Song

import kotlinx.serialization.Serializable
import kotlin.math.pow
import com.sawears.data.midijson.MidiJsonSongNoteDto

/**
 * Domain layer note representation
 *
 * @property pitchMidi pitch as a midi value
 * @property durationRelative note duration expressed relative to a single beat
 */
@Serializable
data class Note(
    val pitchMidi: Int,
    val durationRelative: Double
){
    /**
     * Convert relative duration to absolute ms duration given tempo in BPM
     *
     * @tempoBPM
     */
    fun toMs(tempoBPM: Int): Double{
        return durationRelative * (60_000.0/tempoBPM)
    }
    
    /**
     * Convert midi pitch to Hz
     */
    fun toHz(): Float {
        if (pitchMidi < 0){
            return pitchMidi.toFloat()
        } else {
            return 440 * 2f.pow((pitchMidi-69)/12f)
        }
    }

    /**
     * Transpose the note
     *
     * @param semitones the number of semitones +/- to transpose
     * @return a new Note with updated pitch
     */
    fun transpose(semitones: Int): Note {
        val transposedPitch = pitchMidi + semitones
        if (transposedPitch < 0){
            throw TranspositionException("Pitch $pitchMidi cannot be transposed by $semitones semitones")
        }
        return Note(
            transposedPitch,
            durationRelative
        )
    }
}

/**
 * Error representing a malformed midiJSON file
 */
class TranspositionException(
    message: String,
    cause: Throwable? = null
): Exception(message, cause)

/**
 * Extension function to convert a MidiJsonSongNoteDto -> NoteDto
 */
internal fun MidiJsonSongNoteDto.toNote(): Note {
    return Note(
        pitchMidi,
        durationRelative
    )
}
