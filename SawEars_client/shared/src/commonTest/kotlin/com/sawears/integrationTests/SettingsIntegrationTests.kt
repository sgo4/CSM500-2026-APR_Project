package com.sawears.integrationTests

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sawears.data.MidiJsonRepository
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.mocks.MockSettingsRepository
import com.sawears.model.useCases.RegisterLesson
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.data.database.*
import com.sawears.mocks.mockSong
import com.sawears.ui.screens.lessonsettings.LessonSettingsViewModel
import com.sawears.ui.screens.selectsong.SongSelectionViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class SettingsIntegrationTests{

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp(){
        Dispatchers.setMain(testDispatcher)

    }

    @AfterTest
    fun tearDown(){
        Dispatchers.resetMain()

    }

    /**
     * Lesson settings should be persistent across screens as long as the application is active.
     *
     * This test demonstrates this by simulating the following flow:
     *
     * 1. User selects song on the song selection screen
     * 2. User changes song settings on the lesson settings screen
     * 3. User goes back to the song selection screen later and selects the same song within the same session
     * 4. Updates from step 2 should still be reflected
     */
    @Test
    fun `lesson settings are persistent across multiple screens`() = runTest {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        val appSettings = AppSettings(
            rangeMinMidi = 70,
            rangeMaxMidi = 90,
            mode=LessonMode.PRACTICE
        )
        val settingsRepository = MockSettingsRepository(
            appSettings
        )

        val lessonSettingsRepository = EphemeralLessonSettingsRepository()


        // simulate song selection
        var songSelectionViewModel = SongSelectionViewModel(
            dao= db.songDao(),
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository,
            globalSettingsRepository = settingsRepository
        )

        val songId = 2
        val resourcePath = "files/midijson/avemaria.json"

        songSelectionViewModel.selectSong(2,resourcePath)
        advanceUntilIdle()


        // simulate changing song settings
        var settingsViewModel = LessonSettingsViewModel(
            lessonId = songId,
            lessonSettingsRepository = lessonSettingsRepository,
            settingsRepository = settingsRepository,
            autoTranspose = true
        )
        settingsViewModel.updateTempo(400)

        // simulate selecting song again
        // initialize a new song selection viewModel
        songSelectionViewModel = SongSelectionViewModel(
            dao= db.songDao(),
            songRepository = MidiJsonRepository(),
            lessonSettingsRepository = lessonSettingsRepository,
            globalSettingsRepository = settingsRepository
        )
        songSelectionViewModel.selectSong(2,resourcePath)
        advanceUntilIdle()

        // open a new lesson settings model
        settingsViewModel = LessonSettingsViewModel(
            lessonId = songId,
            lessonSettingsRepository = lessonSettingsRepository,
            settingsRepository = settingsRepository,
            autoTranspose = true
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.lessonSettings.collect {}
        }
        advanceUntilIdle()
        // check that previous settings update is still registered for lesson
        assertEquals(
            400,
            settingsViewModel.lessonSettings.value.tempo
        )
    }
}