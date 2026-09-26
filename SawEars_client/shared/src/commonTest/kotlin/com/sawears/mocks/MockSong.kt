package com.sawears.mocks

import com.sawears.domain.Note
import com.sawears.domain.Song


fun mockSong(
    pitches: List<Int> = listOf(1),
    durations: List<Int>? = null,
    tempo: Int = 80,
    description: String ="",
    year: Int = 1999,
    title: String = "",
    composers: List<String> = listOf("")
): Song {

    // if durations provided ensure list is same size as pitches
    durations?.let {
        require(pitches.size == durations.size)
    }

    // create notes from pitches and durations
    val notes = pitches.zip(
        durations
            ?: List(pitches.size) {1} // if no durations provided generate mock list of 1 durations
    ) { a, b ->
        Note(a.toFloat(), b.toDouble())
    }

    return Song(
        title=title,
        composers=composers,
        year=year,
        notes=notes,
        tempo=tempo,
        description=description
    )
}