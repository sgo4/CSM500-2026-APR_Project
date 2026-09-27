package com.sawears.algorithm_development

import com.sawears.algorithm_development.pitch.YinEstimator
import com.sawears.algorithm_development.io.*
import com.sawears.algorithm_development.pitch.ConstantEstimator
import com.sawears.algorithm_development.pitch.RTSwipeEstimator
import com.sawears.algorithm_development.evaluation.centsDifference
import com.sawears.algorithm_development.evaluation.windowSizeExperiment
import java.io.File

///*
//For diagnostic purposes only, test estimator against a known sinewave.
// */
//fun testWithFixedWaveFile(){
//    println("Hello world")
//    println("Starting")
//    val filePath = "C:\\Users\\sotte\\OneDrive\\Documents\\Audacity\\Project\\AB.wav"
//    val wav = wavFile(File(filePath))
//    val frameSize = 1024
//    val bufferedSource = BufferedPCMAudioSource(
//        samples = wav.samples,
//        frameSize = frameSize,
//        sampleRate = wav.sampleRate
//    )
//    val micSource = WindowsMicAudioSource(
//        1024,48000
//    )
//    val yin_estimator = YinEstimator(
//        sampleRate = wav.sampleRate.toFloat(),
//        frameSize = frameSize
//    )
//    val constant_estimator = ConstantEstimator(
//        sampleRate = wav.sampleRate.toFloat(),
//        frameSize = frameSize,
//    )
//
//    val swiper = RTSwipeEstimator(
//        sampleRate = wav.sampleRate.toFloat(),
//        frameSize = frameSize
//    )
//
//    val estimator = yin_estimator
//    val audioSource = bufferedSource
//
//    while (true){
//        val frame = audioSource.readFrame()?: break
//        val prediction = estimator.estimate(frame)
//        println(prediction.frequency)
//    }
//}

/**
 * Main entrypoint for running algorithm experiments
 */
fun main(){
    windowSizeExperiment(
        dataSet = "ChoraleBricks",
        windowSizes = listOf(256,512,1024,2048),
        sampleRate = 44_100f
    )
}




