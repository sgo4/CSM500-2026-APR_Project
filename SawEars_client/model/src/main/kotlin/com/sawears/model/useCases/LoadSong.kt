package com.sawears.model.useCases

import com.sawears.model.Song

import com.sawears.data.SongRepository
import com.sawears.model.toSong

/**
 * Use case for loading a Song
 */
class LoadSong(private val songRepository: SongRepository) {

    /**
     * Load a song by name from the song repository
     */
    suspend fun load(name: String): Song? {
        return songRepository.load(name)
    }
}