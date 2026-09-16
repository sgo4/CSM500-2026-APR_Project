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
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
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

    private var _track: AudioTrack? = null


    // Implementation detail: android audio record is capable of providing samples as Shorts, so no need
    // to manually convert bytes or worry about endianness as in the desktop implementation
    override fun writeWindow(): Int {

        val track = getTrack()

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

    /*
    Get or create an audio track
     */
    private fun getTrack(): AudioTrack {
        _track?.let { return it }
        require(minimumBufferSize > 0) {
            "Unsupported audio configuration: sampleRate=$sampleRate, channelConfig={$channelConfig.toString()},  encoding={$encoding.toString()}"
        }
        val track = AudioTrack.Builder()
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

        require(track.state == AudioTrack.STATE_INITIALIZED){
            track.release()
            "Failed to initialize AudioTrack"
        }
        _track = track

        return track
    }

    override fun play(){
        getTrack().play()
    }

    override fun pause(){
        _track?.pause()
    }

    override fun flush() {
        _track?.flush()
    }

    override fun stop(){
        _track?.pause()
        _track?.flush()
        _track?.stop()
    }

    override fun close(){
        _track?.stop() // finish playing before closing
        _track?.release()
        _track = null
    }
}