package com.sawears.model.pitch

import kotlin.math.pow

/**
 * General purpose midi to hz converter
 */
fun midiToHz(pitchMidi: Float): Float {
    if (pitchMidi < 0){
        return pitchMidi
    } else {
        return 440 * 2f.pow((pitchMidi-69)/12f)
    }
}

/**
 * General purpose hz to midi converter
 *
 * TODO
 */
fun hzToMidi(){

}