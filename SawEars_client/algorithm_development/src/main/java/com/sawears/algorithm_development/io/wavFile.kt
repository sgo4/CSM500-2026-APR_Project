package com.sawears.algorithm_development.io

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import java.io.File
import java.io.InputStream

/**
 * A wav file as a set of signed 16-bit mono PCM samples
 *Facilitates testing algorithms from a fixed audio source for comparison
 *
 * @param file input stream of wav data
 * @property samples the file's audio content as an array of normalized PCM samples
 * @property sampleRate the sample rate in Hz
 */
class wavFile(
    file: InputStream
){

    /**
     * Alternate constructor allowing File object to be passed insteam of an input stream
     */
    constructor(file: File): this(file.inputStream())


    /** File audio as an array of normalized PCM samples     */
    val samples: FloatArray
    /** Sample rate in Hz */
    val sampleRate: Int

    init {
        file.use{
            val (s, r) = load(file)
            samples = s
            sampleRate = r
        }
    }

    /*
     * Loads a wav file as a mono, signed 16-bit PCM stream
     *
     * @param file input stream from loaded wav file
     * @return loaded samples + sample rate
    */
    private fun load(file: InputStream): Pair<FloatArray, Int> {
        // convert wav file to audio input stream
        val inputStream = AudioSystem.getAudioInputStream(file)
        // extract base format of input
        val inputFormat = inputStream.format
        /*
        Define desired format for PCM appropriate for wav file on PC:
        16 bit, mono, little-endian
         */
        val pcmFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            inputFormat.sampleRate,
            16,
            1,
            2,
            inputFormat.sampleRate,
            false
        )
        // convert input stream to stream of PCM bytes
        val pcmBytes = AudioSystem.getAudioInputStream(
            pcmFormat,
            inputStream
        ).readBytes()
        // convert raw bytes to array of floats representing signed PCM values
        val pcmSamples = parsePCMByteArray(pcmBytes, bigEndian = false)
        // normalize samples
        for (i in pcmSamples.indices){
            pcmSamples[i] /= 32767f
        }
        // return samples and sample rate
        return pcmSamples to pcmFormat.sampleRate.toInt()
    }

    /*
     Convert bytes to PCM values.
     PCM values are stored as 16-bit little-endian values
     each of which is made up of 2 bytes so converting to
     integers requires:
     1. Parsing the byte array in 2 byte pairs
     2. Identifying the high and low byte for each pair
     3. Converting the low byte to an unsigned integer
     4. Shifting the high byte 8 places
     5. Combining the high and low byte

     @param byteArray
     @param bigEndian boolean representing the endianness of the bytes (big = true)
     */
    private fun parsePCMByteArray(byteArray: ByteArray, bigEndian: Boolean): FloatArray{

        val pcmSamples = FloatArray(byteArray.size / 2)
        var i = 0
        while (i < pcmSamples.size) {
            val lowByteIndex = if (bigEndian) i * 2 + 1 else i * 2
            val highByteIndex = if (bigEndian)  i * 2 else i * 2 + 1
            val lowByte = byteArray[lowByteIndex].toInt() and 0xff
            val highByte = byteArray[highByteIndex].toInt()
            pcmSamples[i] = ((highByte shl 8) or lowByte).toFloat()
            i++
        }
        return pcmSamples
    }
}