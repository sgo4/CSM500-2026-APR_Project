package com.sawears.di

import android.content.Context
import com.sawears.AndroidAppContainer
import com.sawears.AppContainer
import com.sawears.data.MidiJsonRepository
import com.sawears.data.database.AppDatabase
import com.sawears.data.database.buildAndroidDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AndroidAudioInputSourceFactory
import com.sawears.data.io.audioOut.AndroidAudioPlayer
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.pitch.estimators.FixedSineEstimator
import com.sawears.model.useCases.LoadSong
import com.sawears.ui.lesson.components.PlaybackClock
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
 *
 * Tutorials consulted:
 * - https://www.youtube.com/watch?v=bbMsuI2p1DQ (+ source code https://github.com/philipplackner/DaggerHiltCourse)
 * - https://developer.android.com/training/dependency-injection/hilt-android
 *
 */
@Module
@InstallIn(SingletonComponent::class)
object AppProviderModule {

    @Provides
    @Singleton
    fun provideLoadSong(): LoadSong = LoadSong(MidiJsonRepository())


    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer = AndroidAudioPlayer(SineWaveOutputSource())

    @Provides
    @Singleton
    fun provideEstimator(): PitchEstimator = FixedSineEstimator( // TDOD replace with real estimator once build is working
        44_100f,
        1024,
        frequency = 2f,
        minPitcHz = 600,
        maxPitchHz = 1000
    )


    @Provides
    @Singleton
    fun providePlaybackClock(): PlaybackClock = PlaybackClock()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        buildAndroidDatabase(context)

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