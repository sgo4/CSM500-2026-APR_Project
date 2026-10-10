package com.sawears.algorithm_development

import com.sawears.algorithm_development.evaluation.windowSizeExperiment


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




