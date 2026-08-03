package com.sawears.data.midijson

/**
 * Main entry point for model to retrieve midiJSON data
 */
class SongRepository(){
    fun load(name: String): MidiJsonSongDto {
        val midijson = object {}.javaClass.getResourceAsStream("/midijson/${name}.json")?: error("no such json")
        val parser = MidiJsonParser()
        return parser.parse(midijson).toMidiJsonSongDto()
    }
}
