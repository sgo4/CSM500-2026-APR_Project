package com.sawears.algorithm_development.evaluation

import com.sawears.algorithm_development.io.AudioFrame
import com.sawears.algorithm_development.io.AudioSource
import com.sawears.algorithm_development.pitch.PitchEstimator
import com.sawears.algorithm_development.pitch.PitchResult
import java.io.File
import java.io.FileWriter
import kotlin.system.exitProcess

/**
 * Class representing a single experiment
 * Takes an estimator and an audio source and outputs results as csv
 * as (timsestamp, prediction)
 * @param wavFile
 * @property estimator
 */
class Experiment(
    private val audioSource: AudioSource,
    private val estimator: PitchEstimator,
    private val dataSource: String,
    private val audioFileName: String
){

    private var frame_idx = 0
    private val results = mutableListOf<PitchResult>()
    private var processingTime: Double = -1.0
    private val sampleRate = audioSource.sampleRate
    private val frameSize = audioSource.frameSize

    /**
     * Run the experiment on each frame until the audio source is exhausted
     */
    fun run(){
        val timeElapsed = kotlin.system.measureTimeMillis{
            while (true){
                val frame = audioSource.readFrame()
                    ?: break // stop when no frames left to process
                next(frame)
            }
        }
        // record totoal processing time for experiment
        processingTime = timeElapsed.toDouble()


    }

    /**
     * Export results to a master csv
     *
     * @param file a valid csv file object
     */
    fun export(file: File){
        require(file.extension.equals("csv", ignoreCase = true)) {
            "Export path must be a csv"
        }
        // create if not exists
        val isNew = file.createNewFile()

        FileWriter(file, true).buffered().use { out ->
            if (isNew) {
                out.appendLine("dataset;file;estimator;timestamp;f0;processing_time_file") // append header if creating new file
            }

            results.forEach {
                out.appendLine(
                    "$dataSource;" +
                            "$audioFileName;" +
                            "${estimator.javaClass.simpleName};" +
                            "${it.timeStamp};" +
                            "${it.frequency};" +
                            "${processingTime}" // for convenience we append the processing time for the whole file to each sample, to be aggregated during analysis
                )

            }
        }
    }

    /*
    * Process next frame
     */
    private fun next(frame: AudioFrame){
        frame_idx += 1
        val result = estimator.estimate(frame)
        results += PitchResult(
            frequency = result.frequency,
            timeStamp = calculateTimeStamp(), //override timestampe with relative time in recording
            confidence = result.confidence
        )
    }



    /*
    * Calculate (centred) time stamp for a give frame as a function of frame index,
    * frame size and sample rate.
    * Formula: t = (frame_idx + frame_size/2) / sample_rate
     */
    private fun calculateTimeStamp(): Double{
        return (frame_idx*frameSize + frameSize / 2.0) / sampleRate
    }

    // compare each sampled timestamp to its nearest neighbour, or use interpolation
    // it'd be good to roughly align the timestamps by picking a suitable frequency
    // formula = (distance between sample in csv) * sample_rate
    // e.g. distance = 0.006 seconds, sr = 44100, frame size = 264 samples


}