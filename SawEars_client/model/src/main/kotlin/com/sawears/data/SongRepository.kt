package com.sawears.data

import com.sawears.model.Song
import com.sawears.data.midijson.MidiJsonParser
import com.sawears.data.midijson.toMidiJsonSongDto
import com.sawears.model.toSong
import java.io.File


/**
 * Public facing interface for song data, agnostic of source format
 */
interface SongRepository{

	suspend fun load(name: String): Song?

}

/**
 * Loads and parses a midijson resource if present
 */
class MidiJsonRepository(): SongRepository {

    override suspend fun load(name: String): Song? {
        
        val midijson = object {}.javaClass.getResourceAsStream("/midijson/${name}.json") ?: return null
        val parser = MidiJsonParser()
        return parser
            .parse(midijson)
            .toMidiJsonSongDto()
            .toSong()

    }
}
