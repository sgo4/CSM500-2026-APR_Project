package com.sawears.data.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlinx.coroutines.flow.first
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.test.runTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Test class for Room storage model.
 *
 * Note: Per room recommendations, jvm tests are preferred over android local tests,
 * see: https://developer.android.com/training/data-storage/room/testing-db
 *
 * Sources:
 * - https://developer.android.com/training/data-storage/room/testing-db
 * - https://kotest.io/docs/framework/lifecycle-hooks.html
 */
class AppDatabaseTests {

    private lateinit var db: AppDatabase

    @BeforeTest
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun closeDb() {
        db.close()
    }

    /**
     * This test validates upsert behaviour. Upserting should create a new entry
     * and upserting the same entity twice should not create separate entries.
     */
    @Test
    fun `upsert a song`() = runTest {
        val song = SongEntity(
            id=1,
            title="Never Gonna Give You Up",
            year=1987,
            description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            resourcePath="nevergonnagiveyouup.json"
        )


        val songDao = db.songDao()
        val initialCount = songDao.count()
        songDao.upsert(song)
        val countAfterUpsert = songDao.count()
        // check that an entry was created
        assertEquals(initialCount+1, countAfterUpsert)
        // upserting the same song twice shouldn't update count
        songDao.upsert(song)
        assertEquals(countAfterUpsert, songDao.count())

        // song should be retrievable from database
        val res = songDao.searchByTitleExact("Never Gonna Give You Up")

        assertTrue(
            res.any {
                it.title == song.title &&
                        it.year == song.year &&
                        it.description == song.description &&
                        it.resourcePath == song.resourcePath
            }
        )
    }

    /**
     * Test that fuzzy match works. Expected behaviour:
     *
     * - case insensitive
     * - matches string anywhere in word (e.g. 'the' matches "The Wall" and "Like no other")
     */
    @Test
    fun `fuzzy match a song`() = runTest{
        val songDao = db.songDao()
        val testData = listOf(
            SongEntity(
                id=1,
                title="Never Gonna Give You Up",
                year=1987,
                description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                resourcePath="nevergonnagiveyouup.json"
            ),
            SongEntity(
                id=2,
                title="never Gonna Give You Up",
                year=1987,
                description="Should match (case insensitive)",
                resourcePath="nevergonnagiveyouup.json"
            ),
            SongEntity(
                id=3,
                title="Never Gonna Grease You Up",
                year=1987,
                description="Should match (includes substring)",
                resourcePath=""
            ),
            SongEntity(
                id=4,
                title="Gonnanever Grease You Up",
                year=1987,
                description="Should match (includes substring)",
                resourcePath=""
            ),
            SongEntity(
                id=5,
                title="Ne'er Gonna Give Ye Up",
                year=1687,
                description="Should not match",
                resourcePath=""
            )
        )

        testData.forEach { it ->
            songDao.upsert(it)
        }



        val fuzzyMatches = songDao.searchSongsByTitleFuzzy("never").first()

        assertEquals(4,fuzzyMatches.size)

    }

    /**
     * Test composer lookup for both:
     * - multiple songs same composer
     * - multiple composers same song
     */
    @Test
    fun `search by compopser`() = runTest {

        val songDao = db.songDao()


        val songs = listOf(
            SongEntity(
                id=1,
                title="Never Gonna Give You Up",
                year=1987,
                description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                resourcePath="nevergonnagiveyouup.json"
            ),
            SongEntity(
                id=2,
                title="Whenever You Need Somebody",
                year=1987,
                description="A different song by Rick Astley (he wrote more than one!)",
                resourcePath=""
            ),
            SongEntity(
                id=3,
                title="Freebird",
                year=1973,
                description="A song with multiple authors",
                resourcePath=""
            )
        )

        val composers = listOf(
            ComposerEntity(
                id=1,
                name = "Rick Astley"
            ),
            ComposerEntity(
                id=2,
                name="Lynyrd Skynyrd"
            ),
            ComposerEntity(
                id=3,
                name="Allen Collins"
            ),
            ComposerEntity(
                id=4,
                name="Ronnie Van Zant"
            )
        )
        // map songs to composers
        val songComposerLookup = listOf(

            SongComposerLookupEntity(
                songId=1,
                composerId=1
            ),
            SongComposerLookupEntity(
                songId=2,
                composerId=1
            ),
            SongComposerLookupEntity(
                songId=3,
                composerId=2
            ),
            SongComposerLookupEntity(
                songId=3,
                composerId=3
            ),
            SongComposerLookupEntity(
                songId=3,
                composerId=4
            ),
        )

        // add data to db
        songs.forEach { it ->
            songDao.upsert(it)
        }
        composers.forEach { it ->
            songDao.upsert(it)
        }
        songComposerLookup.forEach { it ->
            songDao.upsert(it)
        }

        // test some queries

        // db should contain 2 songs by Rick Astley
        val songsByRickAstley = songDao.getSongsByComposer("Rick Astley").first()
        assertEquals(2,songsByRickAstley.size)

        // db should contain 1 song by Ronnie Van Zant (Freebird)
        val songsByRonnieVanZant = songDao.getSongsByComposer("Ronnie Van Zant").first()
        assertEquals(1,songsByRonnieVanZant.size)
        assertEquals("Freebird",songsByRonnieVanZant[0].title)

        // it should be possible to retrieve both composers for Freebird
        // (Lynyrd Skynyrd, Ronnie Vant Zant, Allen Collins)
        val freebirdComposers = songDao.getComposersForSong(3).first()
        assertEquals(3, freebirdComposers.size)
        assertTrue(
            freebirdComposers.any {
                it.name=="Ronnie Van Zant"
            } &&
            freebirdComposers.any {
                it.name=="Allen Collins"
            } &&
                    freebirdComposers.any {
                        it.name=="Lynyrd Skynyrd"
                    }
        )
    }

    /**
     * Test initial database seeding
     */
    @Test
    fun `adding seed data works as intended`() = runTest{
        val songDao = db.songDao()
        // some token data to seed
        val seedData = SeedData(
            songs=listOf(
                SongEntity(
                    id=1,
                    title="Never Gonna Give You Up",
                    year=1987,
                    description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    resourcePath="nevergonnagiveyouup.json"
                )
            ),
            composers=listOf(
                ComposerEntity(
                    id=1,
                    name = "Rick Astley"
                )
            ),
            lookups = listOf(
                SongComposerLookupEntity(
                    songId=1,
                    composerId=1
                )
            )
        )


        val seedVersion = 1
        val seedDao = db.seedDao()
        // verify that database not previously seeded
        assertFalse(seedDao.seeded(seedVersion))
        seedDatabase(db,seedVersion,seedData)

        // check that the song was seeded
        assertEquals(
            expected=1,
            actual=songDao
                .searchByTitleExact("Never Gonna Give You Up")
                .size
        )

        // check that seed version was updated
        assertTrue(seedDao.seeded(seedVersion))
    }

    /**
     * Tests that successive seeds for same version
     */
    @Test
    fun `database seeding is idempotent`() = runTest{
        val songDao = db.songDao()
        // some token data to seed
        val seedData = SeedData(
            songs=listOf(
                SongEntity(
                    id=1,
                    title="Never Gonna Give You Up",
                    year=1987,
                    description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    resourcePath="nevergonnagiveyouup.json"
                )
            ),
            composers=listOf(
                ComposerEntity(
                    id=1,
                    name = "Rick Astley"
                )
            ),
            lookups = listOf(
                SongComposerLookupEntity(
                    songId=1,
                    composerId=1
                )
            )
        )


        val seedVersion = 1
        val seedDao = db.seedDao()

        // seed data base twice in succession
        seedDatabase(db,seedVersion,seedData)
        seedDatabase(db,seedVersion,seedData)

        // check that the song was seeded once
        assertEquals(
            expected=1,
            actual=songDao
                .searchByTitleExact("Never Gonna Give You Up")
                .size
        )

        // check that seed version was updated
        assertTrue(seedDao.seeded(seedVersion))
    }

    @Test
    fun `updating seed version`() = runTest {

        val songDao = db.songDao()
        // some token data to seed
        val seedData = SeedData(
            songs=listOf(
                SongEntity(
                    id=1,
                    title="Never Gonna Give You Up",
                    year=1987,
                    description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    resourcePath="nevergonnagiveyouup.json"
                )
            ),
            composers=listOf(
                ComposerEntity(
                    id=1,
                    name = "Rick Astley"
                )
            ),
            lookups = listOf(
                SongComposerLookupEntity(
                    songId=1,
                    composerId=1
                )
            )
        )

        // seed initial version
        val seedVersion = 1
        val seedDao = db.seedDao()
        seedDatabase(db,seedVersion,seedData)


        // some new token data to seed
        val newSeedData = SeedData(
            songs=listOf(
                SongEntity(
                    id=2,
                    title="The Ballad of Testing SawEars",
                    year=1987,
                    description="https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    resourcePath="nevergonnagiveyouup.json"
                )
            ),
            composers=listOf(
                ComposerEntity(
                    id=2,
                    name = "Soeren Otter-Sharp"
                )
            ),
            lookups = listOf(
                SongComposerLookupEntity(
                    songId=2,
                    composerId=2
                )
            )
        )

        // update seed version
        val updatedSeedVersion = 2
        seedDatabase(db,updatedSeedVersion,newSeedData)

        // check that the new song was seeded
        assertEquals(
            expected=1,
            actual=songDao
                .searchByTitleExact("The Ballad of Testing SawEars")
                .size
        )

        // check that old data not removed
        assertEquals(
            expected=1,
            actual=songDao
                .searchByTitleExact("Never Gonna Give You Up")
                .size
        )

        // check that seed version was updated
        assertTrue(seedDao.seeded(updatedSeedVersion))
    }
}


