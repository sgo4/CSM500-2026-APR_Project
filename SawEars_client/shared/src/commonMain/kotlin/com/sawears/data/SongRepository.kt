package com.sawears.data

import com.sawears.model.Song
import com.sawears.data.midijson.MidiJsonParser
import com.sawears.data.midijson.toMidiJsonSongDto
import com.sawears.model.toSong
import sawears.shared.generated.resources.Res


/**
 * Public facing interface for song data, agnostic of source format
 */
interface SongRepository{

	suspend fun load(filepath: String): Song?

}

/**
 * Loads and parses a midijson resource if present
 */
class MidiJsonRepository(): SongRepository {

    /**
     * Loads requested resource based on provided filepath
     * Intended usage is to retrieve the filepath from an index database
     *
     * @param filepath
     * @return parsed midijosn data as a Song object
     */
    override suspend fun load(filepath: String): Song? {
        
        val midijson = Res
            .readBytes(filepath)
            .decodeToString()
        val parser = MidiJsonParser()
        return parser
            .parse(midijson)
            .toMidiJsonSongDto()
            .toSong()
    }
}
