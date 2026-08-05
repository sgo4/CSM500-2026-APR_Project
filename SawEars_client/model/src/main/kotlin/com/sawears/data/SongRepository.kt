package com.sawears.data


/**
 * Public facing interface for song data, agnostic of source format
 */
interface SongRepository(){

	suspend fun load(name: String): Song?

}

/**
 * Loads and parses a midijson resource if present
 */
class MidiJsonRepository(): SongRepository {

    override suspend fun load(name: String): Song? {

        try {
            val midijson = object {}.javaClass.getResourceAsStream("/midijson/${name}.json") ?: error("no such json")
            val parser = MidiJsonParser()
            return parser.parse(midijson).toMidiJsonSongDto()
        } finally {
            return null
        }
    }
}
