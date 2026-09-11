package com.sawears.model.pitch

import kotlin.math.pow

/**
 * General purpose midi to hz converter
 *
 * @param pitchMidi
 * @return pitch in Hz
 */
fun midiToHz(pitchMidi: Float): Float {
    return if (pitchMidi < 0){
        pitchMidi
    } else {
        440 * 2f.pow((pitchMidi-69)/12f)
    }
}