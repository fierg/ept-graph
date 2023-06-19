package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.PeriodAnalyzer
import io.github.fierg.data.FileReader
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.model.CompositionMode
import io.github.fierg.periodic.Periodic

fun main(){
    val array = arrayOf(true,false,false,true,true,false).toBooleanArray()
    val period = Periodic().findShortestPeriod(array)
    println("period is $period")

    val number = 24
    println("Factors of $number are ${number.factorsSequence().toList()}")

    val periods = Decomposer().findCover(array)
    periods

    Decomposer().findCover(BooleanArray(16))

    val f2fGraph = FileReader().getF2FNetwork(4)
    val decomposition = Decomposer(state = false, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0, skipSingleStepEdges = true)
    val decompositionResult = decomposition.findComposite(f2fGraph)


    val plot = PeriodAnalyzer.analyzeGraph(decompositionResult)
    PeriodAnalyzer.showPlot(PeriodAnalyzer.createPlot(plot))

}