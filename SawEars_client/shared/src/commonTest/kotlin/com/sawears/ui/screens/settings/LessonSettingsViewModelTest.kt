package com.sawears.ui.screens.settings

import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.mocks.MockSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.mocks.mockSong
import com.sawears.model.settings.Difficulty
import com.sawears.model.useCases.RegisterLesson
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.lessonsettings.LessonSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals


@OptIn(ExperimentalCoroutinesApi::class)
class LessonSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var appSettings: AppSettings? = null
    private var settingsRepository: SettingsRepository? = null
    private var lessonSettingsRepository: LessonSettingsRepository? = null
    private var viewModel: LessonSettingsViewModel? = null

    private var registerLesson: RegisterLesson? = null


    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

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
    fun tearDown() {
        Dispatchers.resetMain()
        lessonSettingsRepository = null
        viewModel = null
    }

    /**
     * Before starting a lesson, it must be registerd with the lesson settings repository.
     * This behaviour verifies that the [LessonSettingsViewModel] cannot be initialized
     * without first registering the lesson.
     */
    @Test
    fun `initialization throws error if lessonId not registered`() = runTest {

        val exception = assertFailsWith<IllegalStateException> {
            val settingsRepository = MockSettingsRepository(
                AppSettings(
                    rangeMinMidi = 70,
                    rangeMaxMidi = 90
                )
            )

            val viewModel = LessonSettingsViewModel(
                lessonId = 1,
                lessonSettingsRepository = EphemeralLessonSettingsRepository(),
                settingsRepository = settingsRepository,
                autoTranspose = true
            )
        }

        assertEquals("Unable to find settings for lesson 1", exception.message)
    }

    /**
     * If autotransposition flag set to true, any changes to range min or range max
     * should have the effect of centring the song's melody on the new range
     */
    @Test
    fun `autotransposition works for valid ranges`() = runTest {

        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60)
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        advanceUntilIdle()

        // change range
        viewModel.updateRangeMax(99)
        advanceUntilIdle()
        viewModel.updateRangeMin(97)
        advanceUntilIdle()

        // song note should now be exactly equidistant between range max and min
        assertEquals(
            98f,
            viewModel.lessonSettings.value.song.notes.get(0).pitchMidi
        )

    }

    /**
     * If user sets instrument range smaller than the range of the current song,
     * expected behaviour is to refuse transposition and update transposition status
     * but to allow the change to go through
     */
    @Test
    fun `autotransposition rejects invalid ranges`() = runTest {
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60,70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        advanceUntilIdle()

        // change range to be < song range (initial defaults are 70 - 90
        viewModel.updateRangeMax(79)
        advanceUntilIdle()

        // check that change was allowed
        assertEquals(79,viewModel.lessonSettings.value.rangeMax)

        // check that transposition did not occur
        assertEquals(
            listOf(60f,70f),
            viewModel.lessonSettings.value.song.notes.map { it.pitchMidi }
        )

        // check that transposition status updated
        assertFalse(viewModel.transpositionStatus.value)

    }

    @Test
    fun `update tempo updates at lesson level only`() = runTest{
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60,70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.settings.collect {}
        }

        advanceUntilIdle()

        viewModel.updateTempo(1000)
        advanceUntilIdle()

        // verify update went through to lesson settings
        assertEquals(
            1000,
            viewModel.lessonSettings.value.tempo
        )
    }


    /**
     * For tempo, invalid input is any tempo < 1
     * No hard upper limit is imposed on tempo
     */
    @Test
    fun `update tempo rejects invalid input`() = runTest {
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60, 70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.lessonSettings.collect {}
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        advanceUntilIdle()

        // test both 0 and negative input
        val invalidTempos = listOf(0, -1)
        
        invalidTempos.forEach { tempo ->
            viewModel.updateTempo(tempo)
            advanceUntilIdle()
            // verify update was not accepted to lesson settings
            assertNotEquals(
                tempo,
                viewModel.lessonSettings.value.tempo
            )
            // verify expected error state
            assertEquals(
                SettingsError.InvalidTempo,
                viewModel.error.value
            )
        }
    }

    @Test
    fun `update range updates at both lesson and app level`() = runTest{
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60,70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.settings.collect {}
        }

        advanceUntilIdle()

        // update range
        viewModel.updateRangeMax(
            100
        )
        advanceUntilIdle()
        viewModel.updateRangeMin(
            99
        )
        advanceUntilIdle()

        // verify update went through to both lesson and app settings
        assertEquals(
            listOf(99,100),
            listOf(
                viewModel.lessonSettings.value.rangeMin,
                viewModel.lessonSettings.value.rangeMax
            )
        )
        assertEquals(
            listOf(99,100),
            listOf(
                viewModel.settings.value.rangeMinMidi,
                viewModel.settings.value.rangeMaxMidi
            )
        )
    }

    @Test
    fun `update mode updates at both lesson and app level`() = runTest{
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60,70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.settings.collect {}
        }

        advanceUntilIdle()

        // update mode (default is PRACTICE)
        viewModel.updateMode(
            LessonMode.PERFORMANCE
        )
        advanceUntilIdle()

        // verify update went through to both lesson and app settings
        assertEquals(
            LessonMode.PERFORMANCE,
            viewModel.lessonSettings.value.mode
        )
        assertEquals(
            LessonMode.PERFORMANCE,
            viewModel.settings.value.mode
        )
    }

    @Test
    fun `update pitch margin updates at both lesson and app level`() = runTest {
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(60,70) // 10 notes apart
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.settings.collect {}
        }

        advanceUntilIdle()

        viewModel.updatePitchMargin(100)
        advanceUntilIdle()

        // verify update went through to both lesson and app settings
        assertEquals(
            100,
            viewModel.lessonSettings.value.pitchDisplayMarginMidi
        )
        assertEquals(
            100,
            viewModel.settings.value.pitchDisplayMarginMidi
        )
    }
}