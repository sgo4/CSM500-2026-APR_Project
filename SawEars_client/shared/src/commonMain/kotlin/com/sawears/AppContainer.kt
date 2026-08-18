package com.sawears

import androidx.room3.RoomDatabase
import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.ui.lesson.components.PlaybackClock
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.useCases.LoadSong

interface AppContainer {
    val loadSong: LoadSong
    val audioPlayer: AudioPlayer
    val estimator: PitchEstimator
    val audioInputSource: AudioInputSource
    val playbackClock: PlaybackClock
    val db: AppDatabase

}
