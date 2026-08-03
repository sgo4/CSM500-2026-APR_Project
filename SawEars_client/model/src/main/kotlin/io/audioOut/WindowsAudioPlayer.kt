package com.sawears.model.io.audioOut

import java.io.Closeable
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import com.sawears.data.midijson.SongRepository
import com.sawears.model.Song.toSong

/**
 * Class for simple sine wave generator
 *
 * TODO move this to a platform specific folder (or simply delete)
 */
class WindowsSineWaveGenerator(
    val audioSource: PCMAudioOutputSource
): Closeable{
    val sampleRate = audioSource.sampleRate
    val windowSize = (sampleRate * 0.02).toInt() // calculate window to deliver audio in ~0.2 ms chunks
    private val byteWindow = ByteArray(windowSize * Short.SIZE_BYTES)
    private val buffer = ShortArray(windowSize)
    private val format = AudioFormat(
        sampleRate,
        16,
        1,
        true,
        false
    )
    private val line = AudioSystem.getSourceDataLine(format)

    init {
        val internalBufferSize = byteWindow.size * 4

        line.open(format, internalBufferSize)
        line.start()
    }

    fun writeWindow(): Int {

        val length = audioSource.readIntoBuffer(buffer)

        require(length <= windowSize)

        var byteIdx = 0

        for (sampleIdx in 0 until length){
            val sampleValue = buffer[sampleIdx].toInt()

            byteWindow[byteIdx++] = sampleValue.toByte()
            byteWindow[byteIdx++] = (sampleValue shr 8).toByte()
        }
        writeFully(byteWindow, byteIdx)
        return length
    }

    private fun writeFully(
        bytes: ByteArray,
        length: Int
    ) {
        var offset = 0

        while (offset < length) {
            val written = line.write(
                bytes,
                offset,
                length - offset
            )

            check(written > 0) {
                "Audio output wrote no data"
            }

            offset += written
        }
    }

    /**
     * Play all played audio audio
     */
    fun drain() {
        line.drain()
    }

    /**
     * Discard unplayed audio
     */
    fun flush() {
        line.flush()
    }

    override fun close(){
        line.drain()
        line.stop()
        line.close()
    }
}



fun main(){
    val name = "test"
    var song = SongRepository().load(name).toSong()

    var t = 0
    repeat(4){
        println("transpose $t")
        song=song.transpose(t)
        t += 2
        val audioSource = PCMAudioOutputSource(
            song
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