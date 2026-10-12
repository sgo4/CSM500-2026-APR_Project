package com.sawears.mocks

import com.sawears.data.io.audioIn.AudioWindow
import com.sawears.domain.pitch.PitchEstimator
import com.sawears.domain.pitch.PitchResult

class MockEstimator(): PitchEstimator {
    override val sampleRate: Float
        get() = TODO("Not yet implemented")
    override val windowSize: Int
        get() = TODO("Not yet implemented")

    override fun estimate(frame: AudioWindow): PitchResult {
        TODO("Not yet implemented")
    }
}