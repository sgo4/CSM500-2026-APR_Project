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
}