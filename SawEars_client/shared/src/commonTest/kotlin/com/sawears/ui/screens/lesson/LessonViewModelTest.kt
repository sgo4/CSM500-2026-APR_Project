package com.sawears.ui.screens.lesson

import com.sawears.mocks.MockSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.settings.SettingsViewModel
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


// TODO move folder
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
}