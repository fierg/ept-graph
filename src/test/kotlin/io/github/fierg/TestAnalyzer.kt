package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.F2FReader
import io.github.fierg.model.CompositionMode
import io.github.fierg.model.PlotType
import org.junit.Test

class TestAnalyzer {

    @Test
    fun testAnalyzerEdge(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val plot = Visualizer.analyzePeriods(periods)
        Visualizer.savePlotToFile("test-edge-plot.png", Visualizer.createPlotFromOccurrences(plot, PlotType.GEOM_POINT, "Periods of Network 0 edge 6"),"./test-plots")
    }

    @Test
    fun testAnalyzerGraph(){
        val f2fGraph = F2FReader().getF2FNetwork(6)
        val decomposition = Decomposer(state = true, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0, skipSingleStepEdges = true)
        val decompositionResult = decomposition.findComposite(f2fGraph)


        val plot = Visualizer.analyzeGraph(decompositionResult)
        Visualizer.savePlotToFile("test-graph-plot.png", Visualizer.createPlotFromOccurrences(plot, PlotType.GEOM_BAR, "Periods of Network 6"), "./test-plots")
    }


    @Test
    fun testAnalyzerAllGraphs(){
        val options = DotEnvParser.readDotEnv()
        val evalResult = Visualizer.analyzeAllGraphs(Decomposer(options),1)
        Visualizer.savePlotToFile("test-all-graphs-bar-char.png", Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_BAR, "All Periods of all Graphs"), "./test-plots")
        Visualizer.savePlotToFile("test-all-graphs-point-plot.png", Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_POINT, "All Periods of all Graphs"), "./test-plots")
        Visualizer.savePlotToFile("test-all-covered-values-pie-chart.png", Visualizer.createPieChartOfOccurrences(evalResult), "./test-plots")
    }
}