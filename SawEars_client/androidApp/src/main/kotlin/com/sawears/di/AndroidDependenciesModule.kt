package com.sawears.di

import android.content.Context
import com.sawears.AndroidAppContainer
import com.sawears.AppContainer
import com.sawears.data.MidiJsonRepository
import com.sawears.data.SongRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.database.buildAndroidDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AndroidAudioInputSourceFactory
import com.sawears.data.io.audioOut.AndroidAudioPlayer
import com.sawears.data.io.audioOut.AndroidAudioPlayerFactory
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.data.settings.DataStoreSettingsRepository
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.data.settings.createDataStore
import com.sawears.diagnostics.NoOpRealTimeLatencyTracker
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.model.pitch.FastYinEstimator
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.pitch.estimators.FixedSineEstimator
import com.sawears.model.settings.StandardDifficultySettingsProvider
import com.sawears.ui.screens.lesson.components.PlaybackClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * Hilt dependency injection module for android application
 *
 * Tutorials consulted:
 * - https://www.youtube.com/watch?v=bbMsuI2p1DQ (+ source code https://github.com/philipplackner/DaggerHiltCourse)
 * - https://developer.android.com/training/dependency-injection/hilt-android
 */
@Module
@InstallIn(SingletonComponent::class)
object AppProviderModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = DataStoreSettingsRepository(createDataStore(context))

    @Provides
    @Singleton
    fun provideLocalSettingsRepository(): LessonSettingsRepository =
        EphemeralLessonSettingsRepository()

    @Provides
    @Singleton
    fun provideSongRepository(): SongRepository = MidiJsonRepository()


    @Provides
    @Singleton
    fun provideAudioPlayerFactory(): AudioPlayerFactory = AndroidAudioPlayerFactory()

    @Provides
    @Singleton
    fun provideEstimator(): PitchEstimator = FastYinEstimator(
        44_100f,
        1024
    )

    @Provides
    @Singleton
    fun providePlaybackClock(): PlaybackClock = PlaybackClock()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        buildAndroidDatabase(context)

    @Provides
    @Singleton
    fun provideLatencyTracker(): RealTimeLatencyTracker = NoOpRealTimeLatencyTracker()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAudioInputSourceFactory(
        implementation: AndroidAudioInputSourceFactory
    ): AudioInputSourceFactory

    @Binds
    @Singleton
    abstract fun bindAppContainer(
        implementation: AndroidAppContainer
    ): AppContainer

    @Binds
    @Singleton
    abstract fun bindDifficultySettingsProvider(
        implementation: StandardDifficultySettingsProvider
    )

}