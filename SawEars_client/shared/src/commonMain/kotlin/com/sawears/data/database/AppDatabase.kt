package com.sawears.data.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

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

fun getSeedVersion(): Int {
    return 1
}
/**
 * Function to seed startup data
 *
 * @param db an [AppDatabase] object to seed
 * @param seedVersion version id to track updates
 * @param seedData a [SeedData] object containing data to seed
 */
suspend fun seedDatabase(
    db: AppDatabase,
    seedVersion: Int = getSeedVersion(),
    seedData: SeedData = getSeedData()
): AppDatabase {

    // seed data if needed
    val seedDao = db.seedDao()

    // add seed data (if not already seeded)
    addSeedData(seedVersion, seedDao, seedData)

    return db
}

/**
 * Seeds initial local data for songs packaged with the application
 *
 * @param seedVersion used to allow future updates to be pushed while
 *  avoiding seeding the same data multiple times
 * @param seedDao a [SeedDao] to handle database operations
 * @param seedData a [SeedData] object containing data to seed
 */
suspend fun addSeedData(
    seedVersion: Int,
    seedDao: SeedDao,
    seedData: SeedData
){
    // terminate if already seeded
    if (seedDao.seeded(seedVersion)){
        return
    }

    seedDao.seedSongs(seedData.songs)
    seedDao.seedComposers(seedData.composers)
    seedDao.seedSongComposerLookup(seedData.lookups)

    seedDao.updateSeedVersion(
        DatabaseMetadataEntity(
            seedVersion = seedVersion
        )
    )
}

/**
 * Represents the initial data seeded to the database on app install
 *
 * @property songs a list of [SongEntity]
 * @property composers a list of [ComposerEntity]
 * @property lookups a list of [SongComposerLookupEntity]
 */
data class SeedData(
    val songs: List<SongEntity>,
    val composers: List<ComposerEntity>,
    val lookups: List<SongComposerLookupEntity>
)

/**
 * Determines what data specifically gets seeded on app installation
 */
private fun getSeedData(): SeedData {
    val songs = listOf(
        SongEntity(
            id=1,
            title="Happy Birthday",
            year=1939,
            description ="Traditional",
            resourcePath="files/midijson/happybirthday.json"
        ),
        SongEntity(
            id=2,
            title="Ave Maria",
            year=1825,
            description="Originally written under the title 'Ellens dritter Gesang', the work is today more commonly performed as Ave Maria. Sourced from arrangemnent found at: https://www.free-scores.com/PDF_EN/schubert-franz-peter-ave-maria-30236.pdf",
            resourcePath="files/midijson/avemaria.json"
        ),
        SongEntity(
            id=3,
            title="Hark the Herald Angels Sing",
            year=1939,
            description ="Traditional",
            resourcePath="files/midijson/harktheheraldangelssing.json"
        ),
    )

    val composers = listOf(
        ComposerEntity(
            id=1,
            name="Mildred J. Hill"
        ),
        ComposerEntity(
            id=2,
            name="Patty S. Hill"
        ),
        ComposerEntity(
            id=3,
            name="Franz Schubert"
        ),
        ComposerEntity(
            id=4,
            name="Charles Wesley"
        )
    )

    // map songs to composers
    val lookups = listOf(
        SongComposerLookupEntity(
            songId=1,
            composerId=1
        ),
        SongComposerLookupEntity(
            songId=1,
            composerId=2
        ),
        SongComposerLookupEntity(
            songId=2,
            composerId=3
        ),
        SongComposerLookupEntity(
            songId=3,
            composerId=4
        )
    )
    return SeedData(songs,composers,lookups)
}

