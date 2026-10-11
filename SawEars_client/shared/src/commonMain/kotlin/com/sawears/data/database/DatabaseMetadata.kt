package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Entity representing the database's own metadata
 *
 * @property key used to establish authorative metadata version
 * @property seedVersion keeps track of which version of the seed data
 * was last seeded to prevent repeat seeding
 */
@Entity
data class DatabaseMetadataEntity(
    @PrimaryKey
    val key: Int = 1,
    val seedVersion: Int
)