package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey


/**
 * Defines the main data entity for storing composer information
 *
 * This is necessary as there is a many-to-many relationship between songs
 * and composers.
 *
 * There is intentionally no corresponding dao as the intended usage is to enable
 * accessing songs by composer rather than accessing composers directly.
 *
 * See:
 * - https://developer.android.com/kotlin/multiplatform/room
 * - https://developer.android.com/training/data-storage/room/relationships/many-to-many
 */
@Entity
data class ComposerEntity (
    @PrimaryKey(autoGenerate=true)
    val id: Int = 0,
    val name: String
)
