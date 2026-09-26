package com.sawears.ui.screens.settings

import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.mocks.MockSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.mocks.mockSong
import com.sawears.domain.useCases.RegisterLesson
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


/**
 * Test the expected behaviour of the lesson settings view model
 *
 * App wide settings (range, mode, difficulty, pitch margin) should be
 * updated both at the app and lesson settings level, handling invalid input appropriately.
 *
 * Song specific settings such as tempo should only be updated at lesson settings level.
 *
 * This view model also handles transposition in accordance with project objective O7, the
 * expected behaviour of which is for the melody to automatically be transposed to be centred
 * on the instrument range (where possible) whenever the range is updated and when the view model
 * is initialized on song selection.
 */
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
        appSettings = null
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

            val viewModel = LessonSettingsViewModel(
                lessonId = 1,
                lessonSettingsRepository = EphemeralLessonSettingsRepository(),
                settingsRepository = settingsRepository!!,
                autoTranspose = true
            )
        }

        assertEquals("Unable to find settings for lesson 1", exception.message)
    }

    @Test
    fun `initialization triggers autotransposition if flag set`() = runTest {
        // register lesson for mock song
        val lessonId = 1
        val song = mockSong(
            pitches = listOf(0) // choose initial pitch not within default range
        )
        registerLesson!!.register(
            song,
            lessonId
        )

        // initialize viewmodel
        val viewModel = LessonSettingsViewModel(
            lessonId = 1,
            lessonSettingsRepository = lessonSettingsRepository!!,
            settingsRepository = settingsRepository!!,
            autoTranspose = true // auto transposition flag set to true
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            viewModel.lessonSettings.collect {}
        }
        advanceUntilIdle()

        // song note should now be exactly equidistant between range max and min
        assertEquals(
            80f,
            viewModel.lessonSettings.value.song.notes.get(0).pitchMidi
        )
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

        // viewModel initialization triggers autotransposition so record song notes after transposition
        val initialSongRange = viewModel.lessonSettings.value.song.notes.map { it.pitchMidi }

        // change range to be < song range (initial defaults are 70 - 90, so change max to 79)
        viewModel.updateRangeMax(79)
        advanceUntilIdle()

        // check that change was allowed
        assertEquals(79,viewModel.lessonSettings.value.rangeMax)

        // check that transposition did not occur
        assertEquals(
            initialSongRange,
            viewModel.lessonSettings.value.song.notes.map { it.pitchMidi }
        )

        // check that transposition status updated
        assertFalse(viewModel.transpositionStatus.value)

    }

    @Test
    fun `autotransposition does not occur if flag disabled`() = runTest  {
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
            autoTranspose = false
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

        // song note should be same as initially set
        assertEquals(
            60f,
            viewModel.lessonSettings.value.song.notes.get(0).pitchMidi
        )
    }

    /**
     * Since updating the settings repository is a suspend operation, it's possible
     * for successive updates to be handled incorrectly as updates are not necessarily
     * processed in order.
     *
     * For example, with starting range 70 - 71, increasing rangeMax then rangeMin should
     * be valid and result in range 71-72, but if rangeMin is called before rangeMax update
     * coroutine completes, it will still see the old value of rangeMax, and the resulting range
     * 71- 71 would be classed as invalid.
     *
     * This test ensures the implementation accounts for this, e.g. by serializing updates.
     */
    @Test
    fun `successive range updates do not trigger race condition`() = runTest {
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

        // perform two range updates in sequence with no advanceUntilIdle call, where the second will only be valid
        // after the first completes. In this case raising rangeMin to 97 is only valid AFTER rangeMax
        // has been raised to 99, otherwise rangeMin would be > rangeMax
        viewModel.updateRangeMax(99)
        viewModel.updateRangeMin(97)
        advanceUntilIdle()

        // verify that range min was changed to 97
        assertEquals(
            97,
            viewModel.lessonSettings.value.rangeMin
        )
    }

    /**
     * Tempo is considered to be specific to a given lesson/song so is only expected to be reflected in lesson
     * settings.
     */
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