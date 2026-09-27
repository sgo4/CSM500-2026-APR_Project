package com.sawears.algorithm_development.evaluation

import com.sawears.algorithm_development.io.AudioSource
import com.sawears.algorithm_development.io.BufferedPCMAudioSource
import com.sawears.algorithm_development.io.wavFile
import com.sawears.algorithm_development.pitch.PitchEstimator
import com.sawears.algorithm_development.pitch.RTSwipeEstimator
import com.sawears.algorithm_development.pitch.YinEstimator
import com.sawears.algorithm_development.pitch.DynamicWaveletEstimator
import com.sawears.algorithm_development.pitch.McLeodEstimator
import java.io.File
import kotlin.math.ceil


class DataSetExperimentRunner(
    private val dataSource: String,
    private val estimators: List<PitchEstimator>,
    private val frameSize: Int = 1024,
    private val experimentName: String = "Experiment"
){

    /**
     * Run the experiment on the entire data source
     * Gathers results for all estimators / tracks
     * into a single csv file
     */
    fun run(){

        val exportName = "${dataSource}__${experimentName}.csv"
        File(exportName).delete() // delete file if already exists to avoid multiple appends
        val tracks = parseIndex(dataSource)
        val n_experiments = tracks.size * estimators.size
        println("Running ${n_experiments} experiments (tracks: ${tracks.size} estimators: ${estimators.size})")
        println("Datasource: ${dataSource}")
        println("Estimators:")
        estimators.forEach{
            println(" - ${it.javaClass.simpleName}")
        }
        println("Running:")
        // show a simple loading bar
        val loadingTicks = 50
        var processed = 0
        val experimentsPerTick = ceil(n_experiments/loadingTicks.toFloat()).toInt()
        // conduct experiment per track per estimator
        for (track in tracks){
            for (estimator in estimators){
                var timeElapsed = kotlin.system.measureTimeMillis {
                    val audioSource = getAudioSource(track)
                    val experiment = Experiment(audioSource, estimator, dataSource, track.name)
                    experiment.run()
                    experiment.export(File(exportName))
                }
                estimator.reset()
                processed += 1
                var currentTicks = processed / experimentsPerTick
                print("Time elapsed: ${timeElapsed/1000.0}\n" +"#".repeat(currentTicks) + " ".repeat(loadingTicks - currentTicks) + "($processed/$n_experiments)" +"\r")
            }
        }
    }

    private fun getAudioSource(track: AudioTrack): AudioSource {
        val audioStream = object {}.javaClass.getResourceAsStream(track.path) ?: error("Resource not found")
        val wav = wavFile(audioStream)
        return BufferedPCMAudioSource(
            samples = wav.samples,
            frameSize = frameSize,
            sampleRate = wav.sampleRate
        )
    }

}

/**
 * Parse index for a dataset into a list of AudioTrack objects
 *
 * @param dataSet name of the dataset (matching folder in /resources)
 * @param delimiter the delimiter used in the index csv
 */
fun parseIndex(dataSet: String, delimiter: String = ";"): List<AudioTrack>{
    val index =  object {}.javaClass.getResourceAsStream(
        "/datasets/$dataSet/index.csv"
    ) ?: error("Resource not found")

    val rows = index.bufferedReader().useLines {
            lines ->
        lines
            .drop(1)
            .filter{it.isNotBlank()}
            .map {
                val rowData = it.split(delimiter)
                AudioTrack(
                    rowData[0],
                    rowData[1]
                )
            }
            .toList()
    }
    return rows
}


/**
 * Experiment to test performance with multiple window sizes
 */
fun windowSizeExperiment(
    dataSet: String = "ChoraleBricks",
    windowSizes: List<Int> = listOf(256,512,1024,2048),
    sampleRate: Float = 44_100f
){

    windowSizes.forEach { windowSize ->
        val estimators = listOf(
//        RTSwipeEstimator(
//            sampleRate,
//            windowSize
//        ),
            YinEstimator(
                sampleRate,
                windowSize
            ),
            DynamicWaveletEstimator(
                sampleRate,
                windowSize
            ),
            McLeodEstimator(
                sampleRate,
                windowSize
            )
        )
        val runner = DataSetExperimentRunner(
            dataSet,
            estimators,
            windowSize,
            "windowSize" + windowSize.toString()
        )
        runner.run()
    }
}


