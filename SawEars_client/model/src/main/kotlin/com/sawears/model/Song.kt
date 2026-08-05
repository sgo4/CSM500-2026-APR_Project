package com.sawears.model

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

    /**
     * Transpose song to better fit an instrumental / vocal range
     *
     * @param minMidi the lowest note the instrument / voice can produce
     * @param maxMidi the highest note the instrument / voice can produce
     * @return a new song object with transposed melody centred on target range
     */
    fun adaptForRange(minMidi: Int, maxMidi: Int): Song {

        if (maxMidi <= minMidi){
            // throw error if max not greater than min
            throw TranspositionException(
                "Unable to transpose for range min=$minMidi max=$maxMidi. Max must be greater than min."
            )
        } else if (maxMidi < 0 || minMidi < 0){
            // throw error if range includes negative values
            throw TranspositionException(
                "Transposition failed: target range boundaries must be postitive vs. min=$minMidi max=$maxMidi"
            )
        }

        // calculate song range, only factoring in positive values
        val songMin = notes.filter {it.pitchMidi >= 0}.minOf { it.pitchMidi}
        val songMax = notes.filter {it.pitchMidi >= 0}.maxOf { it.pitchMidi }
        val songRange = songMax - songMin
        val targetRange = maxMidi - minMidi

        // throw exception if song's range is wider than the target range
        if (songRange > targetRange){
            throw TranspositionException(
                "Transposition failed. Range of song ($songRange) is greater than target ($targetRange)"
            )
        }

        // attempt to center melody on instrument range
        val targetRangeCentre = minMidi + (maxMidi - minMidi) / 2
        val songRangeCentre = songMin + (songMax - songMin) / 2
        // calculate distance between centres (in semitones)
        val transpositionDistance = targetRangeCentre - songRangeCentre
        // return new Song object with melody transposed
        return transpose(transpositionDistance)
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