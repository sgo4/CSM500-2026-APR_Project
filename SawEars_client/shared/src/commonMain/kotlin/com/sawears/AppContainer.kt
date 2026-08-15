package com.sawears

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
}
