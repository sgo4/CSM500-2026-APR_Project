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

    @Upsert
    suspend fun upsert(composer: ComposerEntity)

    @Upsert
    suspend fun upsert(songComposer: SongComposerLookupEntity)

    @Query("SELECT count(*) FROM SongEntity")
    suspend fun count(): Int

    /**
     * Get the current list of all songs
     */
    @Query("SELECT * FROM SongEntity")
    fun getAll(): Flow<List<SongEntity>>

    /**
     * Search for exact title. Title may not be unique.
     */
    @Query("""
        SELECT * FROM SongEntity
        WHERE title = :title
    """
    )
    suspend fun searchByTitleExact(title: String): List<SongEntity>

    /**
     * Search for songs matching a specific title (fuzzy)
     */
    @Query("""
        SELECT * 
        FROM SongEntity
        WHERE title LIKE '%' || :titleStem || '%' COLLATE NOCASE
        ORDER BY title
    """
    )
    fun searchSongsByTitleFuzzy(titleStem: String): Flow<List<SongEntity>>

    @Query(
        """
            SELECT SongEntity.*
            FROM SongEntity
            INNER JOIN SongComposerLookupEntity
            ON SongEntity.id = SongComposerLookupEntity.songId
            INNER JOIN ComposerEntity
            ON ComposerEntity.id = SongComposerLookupEntity.composerId
            WHERE ComposerEntity.name = :composer
            ORDER BY SongEntity.title
        """
    )
    fun getSongsByComposer(composer: String): Flow<List<SongEntity>>

    @Query(
        """
    SELECT ComposerEntity.*
    FROM ComposerEntity
    INNER JOIN SongComposerLookupEntity
        ON ComposerEntity.id = SongComposerLookupEntity.composerId
    WHERE SongComposerLookupEntity.songId = :songId
    ORDER BY ComposerEntity.name
    """
    )
    fun getComposersForSong(songId: Int): Flow<List<ComposerEntity>>

}