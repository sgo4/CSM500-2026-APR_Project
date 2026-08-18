package com.sawears.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query


/**
 * Logic for checking db metadata on creation, separated from
 * domain queries
 *
 * Design notes:
 * - these functions are modelled as non-suspend as this work is intended to run only once, when the app
 *   is first started so some additional startup overhead is worth it to avoid needing to add downstream
 *   checks that the work has been completed
 */
@Dao
interface SeedDao {

    /**
     * Check whethter the database has already been seeded with
     * data.
     *
     * The motivation for this as opposed to other checks (e.g. checking if seed data exists)
     * is to avoid re-seeding data that was intentionally removed for whatever reason.
     *
     * By searching for a fixed key = 1, any other metadata items are ignored treating item with key 1
     * as the authoritative metadata source.
     */
    @Query(
        """
            SELECT EXISTS(
            SELECT 1
            FROM DatabaseMetadataEntity
            WHERE seedVersion = :seedVersion AND key = 1
            )
        """
    )
    suspend fun seeded(seedVersion: Int): Boolean

    @Insert
    suspend fun updateSeedVersion(metadataEntity: DatabaseMetadataEntity)

    @Insert
    suspend fun seedSongs(songs: List<SongEntity>)

    @Insert
    suspend fun seedComposers(composers: List<ComposerEntity>)

    @Insert
    suspend fun seedSongComposerLookup(lookups: List<SongComposerLookupEntity>)

}