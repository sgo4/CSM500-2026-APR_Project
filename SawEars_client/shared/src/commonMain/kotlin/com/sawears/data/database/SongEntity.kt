package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json


/**
 * Defines the main data entity for storing songs information
 *
 * Note that this defines the index only, for searching etc. with the resourcePath
 * representing the location of the actual data
 *
 * See: https://developer.android.com/kotlin/multiplatform/room
 */
@Entity
data class SongEntity (
    @PrimaryKey(autoGenerate=true)
    val id: Int = 0,
    val title: String,
    val year: Int,
    val description: String,
    val resourcePath: String? = null
)