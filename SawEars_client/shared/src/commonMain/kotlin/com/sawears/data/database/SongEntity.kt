package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Index


/**
 * Defines the main data entity for storing songs information
 *
 * Note that this defines the index only, for searching etc. with the resourcePath
 * representing the location of the actual data.
 *
 * Implementation notes:
 * - Composer is stored in a separate table to model a many-to-many relationship between song/composer
 * - Id must be provided within the application, this is to facilitate mapping composers to songs
 *
 * Constraints:
 * - to prevent duplication, the combination of title, year, description must be unique
 *
 * @property id a unique id (auto generated)
 * @property title
 * @property year release year
 * @property description
 * @property resourcePath (path to the actual song data in memory)
 *
 * Sources:
 * - https://developer.android.com/kotlin/multiplatform/room
 */
@Entity(
    indices = [
        Index(
            value = ["title", "year", "description"],
            unique = true
        )
    ]
)
data class SongEntity (
    @PrimaryKey(autoGenerate=false)
    val id: Int,
    val title: String,
    val year: Int,
    val description: String,
    val resourcePath: String
)