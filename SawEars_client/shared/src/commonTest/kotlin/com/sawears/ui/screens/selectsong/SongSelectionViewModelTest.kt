package com.sawears.ui.screens.selectsong

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sawears.data.midijson.MidiJsonRepository
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.mocks.MockSettingsRepository
import com.sawears.domain.useCases.RegisterLesson
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.data.database.*
import com.sawears.mocks.mockSong
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.songselection_errors_failed_to_load
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SongSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: AppDatabase

    private var appSettings: AppSettings? = null
    private var settingsRepository: SettingsRepository? = null
    private var lessonSettingsRepository: LessonSettingsRepository? = null

    private var registerLesson: RegisterLesson? = null


    @BeforeTest
    fun setUp(){
        Dispatchers.setMain(testDispatcher)
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        appSettings = AppSettings(
            rangeMinMidi = 70,
            rangeMaxMidi = 90,
            mode=LessonMode.PRACTICE
        )
        settingsRepository = MockSettingsRepository(
            appSettings!!
        )

        lessonSettingsRepository = EphemeralLessonSettingsRepository()

        registerLesson = RegisterLesson(
            appSettings!!,
            lessonSettingsRepository!!
        )
    }

    @AfterTest
    fun tearDown(){
        Dispatchers.resetMain()
        db.close()
        appSettings = null
        lessonSettingsRepository = null
    }

    /**
     * Tests intended behaviour on selecting a song
     *
     * 1. Load song from memory
     * 2. Register with lesson settings repository
     * 3. Emit navigation event to proceed to next page
     */
    @Test
    fun `song selection registers song and emits navigation event`() = runTest {
        // known songId and resource path (taken from seeded db data)
        val songId = 2
        val resourcePath = "files/midijson/avemaria.json"

        val viewModel = SongSelectionViewModel(
            dao= db.songDao(),
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            globalSettingsRepository = settingsRepository!!
        )

        // start listening for navigation events
        val eventListiner = async(start = CoroutineStart.UNDISPATCHED){
            viewModel.events.first()
        }

        viewModel.selectSong(songId,resourcePath)
        advanceUntilIdle()

        // verify that lesson was registered
        val lessonId = songId // lessonId == songId in current implementation
        val registeredLesson = lessonSettingsRepository!!.get(lessonId)
        assertNotNull(
            registeredLesson
        )

        // verify that song was loaded
        val expectedTitle = "Ave Maria"
        val actualTitle = registeredLesson.song.title
        assertEquals(expectedTitle, actualTitle)

        // verify that navigation event emitted
        assertEquals(
            SongSelectionEvent.OpenLessonSettings(lessonId),
            eventListiner.await()
        )
    }

    /**
     * Tests behaviour when invalid resource path provided
     */
    @Test
    fun `song selection does not register nonexistent song and emits error navigation event`() = runTest {
        // song that definitely doesn't exist
        val songId = 99
        val resourcePath = "files/midijson/nevergonnagiveyouup.json"

        val viewModel = SongSelectionViewModel(
            dao= db.songDao(),
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            globalSettingsRepository = settingsRepository!!
        )

        // start listening for navigation events
        val eventListiner = async(start = CoroutineStart.UNDISPATCHED){
            viewModel.events.first()
        }

        viewModel.selectSong(songId,resourcePath)
        advanceUntilIdle()

        // verify that lesson was not registered
        val lessonId = songId // lessonId == songId in current implementation
        val registeredLesson = lessonSettingsRepository!!.get(lessonId)
        assertNull(
            registeredLesson
        )

        // verify that navigation event emitted
        assertEquals(
            SongSelectionEvent.SelectionError(Res.string.songselection_errors_failed_to_load),
            eventListiner.await()
        )
    }

    /**
     * If search bar is blank:
     *
     * 1. All available songs should be exposed
     * 2. Songs should be exposed in alphabetical order
     */
    @Test
    fun `blank query lists all available songs in alphabet order`() = runTest {

        val dao = db.songDao()
        // populate db with some fake songs
        // intentionally populate songs in non-alphabetical
        // order to verify sorting works as expected
        val mockSongTitles = listOf(
            "Song4",
            "Song1",
            "Song3",
            "Song2"
        )

        mockSongTitles.map {
            mockSong(title = it)
        }.forEachIndexed { index, song ->
            dao.upsert(
                SongEntity(
                    id = index,
                    title = song.title,
                    description = song.description ?: "",
                    year = song.year ?: 0,
                    resourcePath = ""
                )
            )
        }
        advanceUntilIdle()
        val viewModel = SongSelectionViewModel(
            dao = dao,
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            globalSettingsRepository = settingsRepository!!
        )

        val results = getSearchResults(viewModel,mockSongTitles.size)

        assertEquals(
            mockSongTitles.sorted(),
            results
        )
    }

    /**
     * Test fuzzy search behaviour:
     *
     * 1. Returns exact matches
     * 2. Returns partial matches (i.e. containing query as substring)
     * 3. Does not return non matches
     */
    @Test
    fun `fuzzy search matches as expected`() = runTest {
        val dao = db.songDao()
        // populate db with some fake songs
        // include some songs with same substring
        val mockSongTitles = listOf(
            "SUBSTRING",
            "containsSUBSTRINGcontains",
            "endswithSUBSTRING",
            "SUBSTRINGstartswith",
            "Song5" // shoud not be returned
        )

        mockSongTitles.map {
            mockSong(title = it)
        }.forEachIndexed { index, song ->
            dao.upsert(
                SongEntity(
                    id = index,
                    title = song.title,
                    description = song.description ?: "",
                    year = song.year ?: 0,
                    resourcePath = ""
                )
            )
        }
        advanceUntilIdle()

        val viewModel = SongSelectionViewModel(
            dao = dao,
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            globalSettingsRepository = settingsRepository!!
        )

        viewModel.searchTitleFuzzy("SUBSTRING")
        advanceUntilIdle()

        // expect to match all strings with SUBSTRING
        val results = getSearchResults(viewModel,4)

        assertEquals(
            listOf(
                "SUBSTRING",
                "containsSUBSTRINGcontains",
                "endswithSUBSTRING",
                "SUBSTRINGstartswith").sorted(),
            results
        )
    }

    /**
     * If search 1 returns songs a,b, c and search 2 returns songs c, d the results after search
     * 1 then search 2 should only be c, d, i.e. the results of the earlier search should not
     * be retained.
     */
    @Test
    fun `sequential searches overwrite previous results`() = runTest {
        val dao = db.songDao()
        // populate db with some fake songs
        // include some songs with same substring
        val mockSongTitles = listOf(
            "SUBSTRING",
            "containsSUBSTRINGcontains",
            "endswithSUBSTRING",
            "SUBSTRINGstartswith",
            "Song5" // shoud not be returned
        )

        mockSongTitles.map {
            mockSong(title = it)
        }.forEachIndexed { index, song ->
            dao.upsert(
                SongEntity(
                    id = index,
                    title = song.title,
                    description = song.description ?: "",
                    year = song.year ?: 0,
                    resourcePath = ""
                )
            )
        }
        advanceUntilIdle()

        val viewModel = SongSelectionViewModel(
            dao = dao,
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            globalSettingsRepository = settingsRepository!!
        )

        // perform two searches in quick succession
        viewModel.searchTitleFuzzy("SUBSTRING")
        viewModel.searchTitleFuzzy("Song5")
        advanceUntilIdle()

        // search results should only reflect the second search
        val results = getSearchResults(viewModel,1)
        assertEquals(
            listOf(
                "Song5"
            ),
            results
        )
    }
}

/**
 * Common logic for getting latest search results from view model flow.
 * Waits for a result to be available with expected number of results.
 *
 * @param viewModel [SongSelectionViewModel]
 * @param expectedSize number of results
 */
internal suspend fun getSearchResults(viewModel: SongSelectionViewModel, expectedSize: Int): List<String>{
    return viewModel.state.map { state ->
        state.songs.map {
            it.title
        }
    }.first{ titles ->
        titles.size == expectedSize
    }
}