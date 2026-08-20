package com.sawears

import com.sawears.data.database.AppDatabase
import com.sawears.data.io.audioIn.AudioInputSource
import com.sawears.data.io.audioOut.AudioPlayer
import com.sawears.model.pitch.PitchEstimator
import com.sawears.model.useCases.LoadSong
import com.sawears.ui.lesson.components.PlaybackClock
import javax.inject.Inject

class AndroidAppContainer
@Inject constructor (
    override val loadSong: LoadSong,
    override val audioPlayer: AudioPlayer,
    override val estimator: PitchEstimator,
    override val audioInputSource: AudioInputSource,
    override val playbackClock: PlaybackClock,
    override val db: AppDatabase,
): AppContainer