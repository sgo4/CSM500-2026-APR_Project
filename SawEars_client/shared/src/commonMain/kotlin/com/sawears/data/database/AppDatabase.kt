package com.sawears.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities = [
        SongEntity::class,
        ComposerEntity::class,
        SongComposerLookupEntity::class,
        DatabaseMetadataEntity::class
               ],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    // use this to verify seeding metadata
    abstract fun seedDao(): SeedDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Common code to create db on app startup
 *
 * @param builder a platform specific builder class
 */
suspend fun createDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    // build db
    val db = builder
        .setDriver(BundledSQLiteDriver())
        .build()

    // seed data if needed
    val SEEDVERSION = 1
    val seedDao = db.seedDao()

    // add seed data (if not already seeded)
    addSeedData(SEEDVERSION, seedDao)

    return db
}

suspend fun addSeedData(seedVersion: Int, seedDao: SeedDao){
    // terminate if already seeded
    if (seedDao.seeded(seedVersion)){
        return
    }

    val songs = listOf(
        SongEntity(
            id=1,
            title="Somewhere Over the Rainbow",
            year=1939,
            description ="Composed for the 1939 feature film 'The Wizard of Oz'. Source: https://www.sheetmusicplus.com/en/product/over-the-rainbow-from-the-wizard-of-oz-21452402.html",
            resourcePath="files/midijson/somewhereovertherainbow.json"
        )
    )

    val composers = listOf(
        ComposerEntity(
            id=1,
            name="Arlen Harold"
        ),
        ComposerEntity(
            id=2,
            name="Harburg Yip"
        )
    )

    val lookups = listOf(
        SongComposerLookupEntity(
            songId=1,
            composerId=1
        ),
        SongComposerLookupEntity(
            songId=1,
            composerId=2
        )
    )
    // add seed data through dao
    seedDao.seedSongs(songs)
    seedDao.seedComposers(composers)
    seedDao.seedSongComposerLookup(lookups)

    //update seed version
    seedDao.updateSeedVersion(
        DatabaseMetadataEntity(
            seedVersion = seedVersion
        )
    )
}


