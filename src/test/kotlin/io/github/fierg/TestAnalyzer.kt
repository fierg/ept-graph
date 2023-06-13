package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.PeriodAnalyzer
import io.github.fierg.data.FileReader
import io.github.fierg.model.CompositionMode
import org.junit.Test

class TestAnalyzer {

    @Test
    fun testAnalyzerEdge(){
        val f2fGraph = FileReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val plot = PeriodAnalyzer.analyzePeriods(periods)
        PeriodAnalyzer.saveToFile("test-edge-plot.html", plot)
    }

    @Test
    fun testAnalyzerGraph(){
        val f2fGraph = FileReader().getF2FNetwork(4)
        val decomposition = Decomposer(state = false, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0, skipSingleStepEdges = true)
        val decompositionResult = decomposition.findComposite(f2fGraph)


        val plot = PeriodAnalyzer.analyzeGraph(decompositionResult)
        PeriodAnalyzer.saveToFile("test-graph-plot.html", plot)
    }
}