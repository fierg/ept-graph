package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.periodic.Periodic

fun main1(){
    val array = arrayOf(true,false,false,true,true,false).toBooleanArray()
    val period = Periodic().findShortestPeriod(array)
    println("period is $period")

    val number = 24
    println("Factors of $number are ${number.factorsSequence().toList()}")

    val periods = Decomposer().findCover(array)

    Decomposer().findCover(BooleanArray(16))

    val f2fGraph = F2FReader().getF2FNetwork(4)
    val decomposition = Decomposer(state = false, deltaWindowAlgo = 0, skipSingleStepEdges = true)
    val decompositionResult = decomposition.findComposite(f2fGraph)
}

/*
fun main(){
    val options = DotEnvParser.readDotEnv()
    val evalResult = Visualizer.analyzeAllGraphs(Decomposer(options), upTo = 1)
    Visualizer.showPlotAsFile(Visualizer.createPlotFromOccurrences(evalResult, options, PlotStyle.GEOM_BAR))
}
*/