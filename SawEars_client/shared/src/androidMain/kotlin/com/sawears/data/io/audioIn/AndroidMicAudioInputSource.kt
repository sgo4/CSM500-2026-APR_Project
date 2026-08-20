package com.sawears.data.io.audioIn

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.math.max

/**
 * Main android audio input source, providing stream of normalised PCM samples
 *
 * @property frameSize the number of samples in each output frame
 * @property sampleRate in Hz
 */
class AndroidMicAudioInputSource
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
constructor(
    override val frameSize: Int = 1024,
    override val sampleRate: Int = 44_100,
): AudioInputSource {

    init {
        require(frameSize > 0) {
            "frameSize must be greater than zero"
        }

        require(sampleRate > 0) {
            "sampleRate must be greater than zero"
        }
    }

    // normalised PCM samples
    private val samples = FloatArray(frameSize)
    // raw PCM buffer
    private val rawSamples = ShortArray(frameSize)

    // android specific channel / encoding configurations
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT

    // track channel open / close
    @Volatile
    private var closed = false

    // android specific audio recorder
    private val recorder: AudioRecord

    init {

        val minimumBufferBytes = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            encoding
        )

        require(minimumBufferBytes > 0) {
            "Unsupported audio configuration: sampleRate=$sampleRate, channelConfig=$channelConfig,  encoding=$encoding"
        }

        val frameBytes = frameSize * 2
        val requestedBufferBytes = max(minimumBufferBytes, frameBytes * 4)

        recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(requestedBufferBytes)
            .build()

        require(recorder.state == AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            "Failed to initialize Android microphone"
        }
        recorder.startRecording()
    }

    /**
     * Blocks until one complete frame has been captured.
     *
     * Callable from a worker thread / dispatchers.IO
     */
    override fun readFrame(): AudioFrame? {
        if (closed) return null

        var offset = 0

        while (offset < frameSize) {
            if (closed) return null

            val count = recorder.read(
                rawSamples,
                offset,
                frameSize - offset,
                AudioRecord.READ_BLOCKING
            )

            if (count < 0) {
                return null
            }

            if (count == 0) {
                continue
            }

            offset += count
        }

        for (sampleIdx in 0 until frameSize) {
            // normalise samples
            samples[sampleIdx] = rawSamples[sampleIdx] / 32768.0f
        }

        // Copy so previously returned frames are not overwritten by the next read.
        return AudioFrame(samples.copyOf(), System.nanoTime())
    }

    override fun close() {
        if (closed) return
        closed = true

        try {
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop()
            }
        } finally {
            recorder.release()
        }
    }
}
