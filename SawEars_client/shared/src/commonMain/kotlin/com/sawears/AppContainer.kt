package com.sawears

import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSourceFactory
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.ui.screens.lesson.components.PlaybackClock
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.useCases.LoadSong

/**
 * Common interface for dependency injection container
 */
interface AppContainer {
    /**
     * Class that loads a song from memory
     */
    val loadSong: LoadSong

    /**
     * Plaform specific audio player class
     */
    val audioPlayer: AudioPlayer

    /**
     * Pitch estimation
     */
    val estimator: PitchEstimator

    /**
     * Factory to provide source of audio input, e.g. from microphone
     */
    val audioInputSourceFactory: AudioInputSourceFactory

    /**
     * Monotonic clock for time synchronisation
     *
     * //TODO work out whether or not to use this
     */
    val playbackClock: PlaybackClock

    /**
     * Factory for app database
     */
    val db: AppDatabase

}
