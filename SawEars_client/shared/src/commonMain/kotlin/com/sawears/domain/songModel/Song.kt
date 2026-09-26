package com.sawears.domain.songModel

import com.sawears.data.midijson.DTOs.MidiJsonSongDto
import kotlin.math.roundToInt


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
    var tempo: Int?,
    val description: String?,
    val notes: List<Note>
) {
    val songMinMidi = notes.filter {it.pitchMidi >= 0}.minOf { it.pitchMidi}
    val songMaxMidi = notes.filter {it.pitchMidi >= 0}.maxOf { it.pitchMidi }

    /**
     * Return a transposed Song object by a defined number of semitones
     */
    fun transpose(semitones: Int): Song {
        val transposedNotes = notes.map {
            it.transpose(semitones)
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

    /**
     * Transpose song to better fit an instrumental / vocal range
     *
     * @param rangeMinMidi the lowest note the instrument / voice can produce
     * @param rangeMaxMidi the highest note the instrument / voice can produce
     * @return a new song object with transposed melody centred on target range
     */
    fun adaptForRange(rangeMinMidi: Int, rangeMaxMidi: Int): Song {

        if (rangeMaxMidi <= rangeMinMidi){
            // throw error if max not greater than min
            throw TranspositionException(
                "Unable to transpose for range min=$rangeMinMidi max=$rangeMaxMidi. Max must be greater than min."
            )
        } else if (rangeMaxMidi < 0 || rangeMinMidi < 0){
            // throw error if range includes negative values
            throw TranspositionException(
                "Transposition failed: target range boundaries must be postitive vs. min=$rangeMinMidi max=$rangeMaxMidi"
            )
        }

        // calculate song range, only factoring in positive values
        val songRange = songMaxMidi - songMinMidi
        val targetRange = rangeMaxMidi - rangeMinMidi

        // throw exception if song's range is wider than the target range
        if (songRange > targetRange){
            throw TranspositionException(
                "Transposition failed. Range of song ($songRange) is greater than target ($targetRange)"
            )
        }

        // attempt to center melody on instrument range
        val targetRangeCentre = rangeMinMidi + (rangeMaxMidi - rangeMinMidi) / 2
        // round to avoid half semitones
        // use roundToInt as this more closely mimics rounding in other languages (i.e. 0.5 rounded up)
        val songRangeCentre = (songMinMidi + (songMaxMidi - songMinMidi) / 2).roundToInt()
        // calculate distance between centres (in semitones)
        val transpositionDistance = targetRangeCentre - songRangeCentre
        // return new Song object with melody transposed
        return transpose(transpositionDistance)
    }

    override fun toString(): String{
        return "title=$title range=($songMinMidi, $songMaxMidi)"
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