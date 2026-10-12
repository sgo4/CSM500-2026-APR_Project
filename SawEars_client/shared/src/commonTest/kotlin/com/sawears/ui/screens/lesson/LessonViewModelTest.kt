package com.sawears.ui.screens.lesson

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sawears.data.database.AppDatabase
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.diagnostics.NoOpRealTimeLatencyTracker
import com.sawears.domain.settings.StandardDifficultySettingsProvider
import com.sawears.domain.useCases.RegisterLesson
import com.sawears.mocks.MockAudioInputSourceFactory
import com.sawears.mocks.MockAudioPlayerFactory
import com.sawears.mocks.MockEstimator
import com.sawears.mocks.MockSettingsRepository
import com.sawears.mocks.mockSong
import com.sawears.ui.screens.lessonsettings.LessonMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest{
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
            mode=LessonMode.PERFORMANCE // ensure playback advances
        )
        settingsRepository = MockSettingsRepository(
            appSettings!!
        )

        lessonSettingsRepository = EphemeralLessonSettingsRepository()

        registerLesson = RegisterLesson(
            appSettings!!,
            lessonSettingsRepository!!
        )
        registerLesson!!.register(
            song= mockSong(
                pitches=listOf(1,1),
                tempo=60 // for ease of calculation 1 beat == 1000ms
            ),
            lessonId = 1
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
     * In this test we verify that pausing lesson playback freezes
     * animation state, by calling onAnimationFrame with a large time delta
     * and verifying that the playback head does not advance. We also test
     * unpaused behaviour to verify that playback head advances while unpaused.
     */
    @Test
    fun `pausing lesson freezes animation`(){

        val viewModel = LessonViewModel(
            lessonId=1,
            audioPlayerFactory = MockAudioPlayerFactory(),
            estimator = MockEstimator(),
            difficultySettingsProvider = StandardDifficultySettingsProvider(),
            audioInputSourceFactory = MockAudioInputSourceFactory(),
            lessonSettingsRepository = lessonSettingsRepository!!,
            latencyTracker = NoOpRealTimeLatencyTracker()
        )

        // advance playback by a large amount when paused == true
        viewModel.onPlaybackFrame(
            frameDeltaNanos = 1_000_000L * 1000,
            latestPitch = null,
            paused = true
        )
        // verify that the playback head has not advanced
        assertEquals(0F,viewModel.frameRenderState.value.playBackHeadBeats )

        // try the same operation when not paused
        viewModel.onPlaybackFrame(
            frameDeltaNanos = 1_000_000L * 1000,
            latestPitch = null,
            paused = false
        )
        //  verify that playback head has advanced by exactly 1 beat
        assertEquals(1F,viewModel.frameRenderState.value.playBackHeadBeats )

    }


}