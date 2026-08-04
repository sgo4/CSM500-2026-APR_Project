package com.sawears.desktop

import com.sawears.desktop.io.audioOut.WindowsSineWaveGenerator
import com.sawears.model.io.audioOut.AudioOutputSource
import com.sawears.model.io.audioOut.SinewWa
import com.sawears.model.useCases.LoadSong

suspend fun main(){
    val name = "somewhereovertherainbow"
    val song = LoadSong().load(name)?: error("Song not found")


    var t = -40
    repeat(4){
        println("transpose $t")
        val transposedsong=song.transpose(t)
        t += 5
        val audioSource: AudioOutputSource = SineWaveOutputSource(
            transposedsong
        )
        val genny = WindowsSineWaveGenerator(
            audioSource
        )

        try{
            genny.use {

                    player ->
                while (true){
                    val windowLength = player.writeWindow()
                    if (windowLength < 1){
                        break
                    }
                }
            }
            genny.drain()
        } finally {
            genny.close()
        }
    }



}