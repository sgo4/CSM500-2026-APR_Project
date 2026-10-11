package com.sawears.data.midijson

import com.sawears.domain.songModel.Song
import com.sawears.data.midijson.parsers.MidiJsonParser
import com.sawears.data.midijson.DTOs.toMidiJsonSongDto
import com.sawears.domain.songModel.toSong
import sawears.shared.generated.resources.Res
import org.jetbrains.compose.resources.MissingResourceException

/**
 * Main entrypoint for domain layer to request [Song] data.
 * Agnostic to details of loading or parsing.
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
        
        val midijson = try {
            Res
                .readBytes(filepath)
                .decodeToString()
        } catch (e: MissingResourceException){
            return null
        }
        val parser = MidiJsonParser()
        return parser
            .parse(midijson)
            .toMidiJsonSongDto()
            .toSong()
    }
}
