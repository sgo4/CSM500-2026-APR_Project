package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class DatabaseMetadataEntity(
    @PrimaryKey
    val key: Int = 1,
    val seedVersion: Int
)