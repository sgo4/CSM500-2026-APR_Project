package com.sawears.desktop

import com.sawears.data.io.audioOut.WindowsSineWaveGenerator
import com.sawears.data.io.audioOut.AudioOutputSource
import com.sawears.data.io.audioOut.SineWaveOutputSource
import com.sawears.model.useCases.LoadSong
import com.sawears.data.MidiJsonRepository
import com.sawears.model.Song

suspend fun loadasongandplayit(){
    val name = "somewhereovertherainbow"
    val song: Song = LoadSong(
        MidiJsonRepository()
    ).load(name)?: error("Song with that name not found")


    var t = -5
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

fun main(){

}