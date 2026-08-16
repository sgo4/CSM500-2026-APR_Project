package com.sawears.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data access object defining allowable queries/operations on song DB.
 *
 * See: https://developer.android.com/kotlin/multiplatform/room
 */
@Dao
interface SongDao {

    @Upsert
    suspend fun upsert(song: SongEntity)

    @Query("SELECT count(*) FROM SongEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM SongEntity")
    fun getAll(): Flow<List<SongEntity>>

    /**
     * Search for songs matching a specific title (fuzzy)
     */
    @Query("""
        SELECT * 
        FROM SongEntity
        WHERE title LIKE '%' || :title || '%'
        ORDER BY title
    """
    )
    fun searchSongsByTitle(title: String): Flow<List<SongEntity>>

    @Query(
        """
            SELECT SongEntity.*
            FROM SongEntity
            INNER JOIN SongComposerLookupEntity
            ON SongEntity.id = SongComposerLookupEntity.songId
            INNER JOIN ComposerEntity
            ON ComposerEntity.id = SongComposerLookupEntity.composerId
            WHERE ComposerEntity.name = :composer
            ORDER BY SongEntity.year
        """
    )
    fun getSongsByComposer(composer: String): Flow<List<SongEntity>>
}