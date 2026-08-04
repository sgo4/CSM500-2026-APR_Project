package com.sawears.model.useCases

import com.sawears.model.Song.Song

import com.sawears.data.midijson.SongRepository
import com.sawears.model.Song.toSong

/**
 * Use case for loading a Song
 */
class LoadSong {

    /**
     * Load a song by name from the song repository
     */
    suspend fun load(name: String): Song? {
        val midiJson = SongRepository().load(name) ?: return null
        return midiJson.toSong()
    }
}