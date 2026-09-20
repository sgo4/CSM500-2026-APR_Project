package com.sawears.ui.screens.settings

import com.sawears.data.settings.AppSettings
import com.sawears.mocks.MockSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
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


@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // range updates
    /**
     * Test range min updates through settingsViewModel with normal input
     */
    @Test
    fun `update rangeMin works with correct unput`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository(
            AppSettings(
                rangeMinMidi = 70, // use known start values for range min / max
                rangeMaxMidi = 80
            )
        )

        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect {}
        }
        advanceUntilIdle()
        // perform a valid update (70 -> 71)
        settingsViewModel.updateRangeMin(71)
        advanceUntilIdle()
        // check that it worked
        assertEquals(71,settingsViewModel.settings.value.rangeMinMidi)
        // error should be clear after valid update
        assertEquals(null,settingsViewModel.error.value)
    }

    /**
     * Test range max updates through settingsViewModel with normal input
     */
    @Test
    fun `update rangeMax works with correct unput`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository(
            AppSettings(
                rangeMinMidi = 70, // use known start values for range min / max
                rangeMaxMidi = 80
            )
        )

        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect {}
        }
        advanceUntilIdle()

        // perform a valid update (80 -> 81)
        settingsViewModel.updateRangeMax(81)
        advanceUntilIdle()

        // check that it worked
        assertEquals(81,settingsViewModel.settings.value.rangeMaxMidi)
        // error should be clear after valid update
        assertEquals(null,settingsViewModel.error.value)
    }

    /**
     * Test expected behaviour if user attempts to set range < 1, i.e. refuse to update
     * and report and error
     */
    @Test
    fun `update range throws error on invalid input`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository(
            AppSettings(
                rangeMinMidi = 70, // use known start values for range min / max
                rangeMaxMidi = 80
            )
        )

        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect {}
        }

        advanceUntilIdle()

        // try to make difference between min and max < 1
        settingsViewModel.updateRangeMax(70)
        advanceUntilIdle()
        // rangeMax should not have updated
        assertEquals(80,settingsViewModel.settings.value.rangeMaxMidi)
        // error should be reported
        assertEquals(SettingsError.RangeLessThanOne, settingsViewModel.error.value)
    }

    // difficulty
    /**
     * Validates that viewModel stores the correct enum in SettingsRepository
     *
     * Tests for actual difficulty behaviour are found under DifficultySettingsTest
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `update difficulty works as expected`() = runTest {

        val settingsRepository: SettingsRepository = MockSettingsRepository()
        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect { }
        }

        val testCases = listOf(
            1 to Difficulty.EASIEST,
            2 to Difficulty.EASY,
            3 to Difficulty.STANDARD,
            4 to Difficulty.HARD,
            5 to Difficulty.HARDEST
        )

        testCases.forEach { (integerLevel, expected ) ->
            settingsViewModel.updateDifficulty(integerLevel)
            advanceUntilIdle()
            assertEquals(expected, settingsViewModel.settings.value.difficulty)
        }
    }

    @Test
    fun `update difficulty fails with invalid values`() = runTest {

        val settingsRepository: SettingsRepository = MockSettingsRepository()
        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )
        assertFailsWith<IllegalArgumentException>{ settingsViewModel.updateDifficulty(0)}
        assertFailsWith<IllegalArgumentException>{ settingsViewModel.updateDifficulty(6)}
    }

    /**
     * Ensures that ordinal mapping among difficulty levels matches assumptions
     * used in lesson view model
     */
    @Test
    fun `difficulty levels map to ENUM in expected order`(){
        assertEquals(
            listOf(1, 2, 3, 4, 5),
            Difficulty.entries.map { it.level }
        )
    }

    // mode
    @Test
    fun `mode updates work as expected`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository()
        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect { }
        }
        advanceUntilIdle()

        val testCase = listOf(
            LessonMode.PRACTICE,
            LessonMode.PERFORMANCE
        )
        testCase.forEach { mode ->
            settingsViewModel.updateMode(mode)
            advanceUntilIdle()
            assertEquals(mode, settingsViewModel.settings.value.mode)
        }
    }

    // pitch margin
    @Test
    fun `pitchMargin works with valid input`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository(
            AppSettings(
                pitchDisplayMarginMidi = 5 // make initial value explicit
            )
        )
        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect { }
        }
        advanceUntilIdle()

        settingsViewModel.updatePitchMargin(10)
        advanceUntilIdle()
        assertEquals(10,settingsViewModel.settings.value.pitchDisplayMarginMidi)

    }

    @Test
    fun `pitchMargin works with invalid input`() = runTest {
        val settingsRepository: SettingsRepository = MockSettingsRepository(
            AppSettings(
                pitchDisplayMarginMidi = 5 // make initial value explicit
            )
        )
        val settingsViewModel = SettingsViewModel(
            settingsRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)){
            settingsViewModel.settings.collect { }
        }
        advanceUntilIdle()

        settingsViewModel.updatePitchMargin(-1)
        advanceUntilIdle()
        assertEquals(5,settingsViewModel.settings.value.pitchDisplayMarginMidi)
        assertEquals(SettingsError.NegativePitchMargin, settingsViewModel.error.value)
    }

}