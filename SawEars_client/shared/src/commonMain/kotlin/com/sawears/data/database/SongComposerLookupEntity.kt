package com.sawears.data.database

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index


/**
 * Junction table supporting song lookup by composer
 */
@Entity(
    primaryKeys = ["songId", "composerId"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComposerEntity::class,
            parentColumns = ["id"],
            childColumns = ["composerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("songId"),
        Index("composerId")
    ]
)
data class SongComposerLookupEntity (
    val songId: Int,
    val composerId: Int
)