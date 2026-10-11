package com.sawears.di

import android.content.Context
import com.sawears.AndroidAppContainer
import com.sawears.AppContainer
import com.sawears.data.midijson.MidiJsonRepository
import com.sawears.data.midijson.SongRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.database.buildAndroidDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioIn.AndroidAudioInputSourceFactory
import com.sawears.data.io.audioOut.AndroidAudioPlayerFactory
import com.sawears.data.io.audioOut.AudioPlayerFactory
import com.sawears.data.settings.DataStoreSettingsRepository
import com.sawears.data.settings.EphemeralLessonSettingsRepository
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.data.settings.createDataStore
import com.sawears.diagnostics.NoOpRealTimeLatencyTracker
import com.sawears.diagnostics.RealTimeLatencyTracker
import com.sawears.domain.pitch.FastYinEstimator
import com.sawears.domain.pitch.McLeodEstimator
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.settings.DifficultySettingsProvider
import com.sawears.domain.settings.StandardDifficultySettingsProvider
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
    fun provideEstimator(): PitchEstimator = McLeodEstimator(
        44_100f,
        1024
    )

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        buildAndroidDatabase(context)

    @Provides
    @Singleton
    fun provideLatencyTracker(): RealTimeLatencyTracker = NoOpRealTimeLatencyTracker()

    @Provides
    @Singleton
    fun difficultySettingsProvider(): DifficultySettingsProvider =
        StandardDifficultySettingsProvider()
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

}