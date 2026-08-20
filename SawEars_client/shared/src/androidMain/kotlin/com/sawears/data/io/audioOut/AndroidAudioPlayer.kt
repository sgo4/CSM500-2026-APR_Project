package com.sawears.data.io.audioOut

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.max

/**
 * Android implementation of audio player interface
 */
class AndroidAudioPlayer(
    override val audioSource: AudioOutputSource
): AudioPlayer {

    val sampleRate = audioSource.sampleRate
    val windowSize = (sampleRate * 0.02).toInt() // calculate window to deliver audio in ~20 ms chunks
    private val buffer = ShortArray(windowSize)

    // android specific channel / encoding configurations
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val format = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(encoding)
        .setChannelMask(channelConfig)
        .build()

    private val minimumBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    // calculate internal buffer based on window size, while also ensuring it meets system requirements
    val internalBufferSize = max(
        minimumBufferSize,
        windowSize * Short.SIZE_BYTES * 4
    )

    private val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(format)
        .setBufferSizeInBytes(internalBufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    init {
        require(minimumBufferSize > 0) {
            "Unsupported audio configuration: sampleRate=$sampleRate, channelConfig={$channelConfig.toString()},  encoding={$encoding.toString()}"
        }

        require(track.state == AudioTrack.STATE_INITIALIZED){
            track.release()
            "Failed to initialize AudioTrack"
        }
    }

    // Implementation detail: android audio record is capable of providing samples as Shorts, so no need
    // to manually convert bytes or worry about endianness as in the desktop implementation
    override fun writeWindow(): Int {

        val length = audioSource.readIntoBuffer(buffer)

        require(length <= windowSize)

        var offset = 0

        while (offset < length){
            val written = track.write(
                buffer,
                offset,
                length - offset,
                AudioTrack.WRITE_BLOCKING
            )

            offset += written
        }
        return length
    }

    override fun play(){
        track.play()
    }

    override fun pause(){
        track.pause()
    }

    override fun flush() {
        track.flush()
    }

    override fun stop(){
        track.pause()
        track.flush()
        track.stop()
    }

    override fun close(){
        track.stop() // finish playing before closing
        track.release()
    }
}