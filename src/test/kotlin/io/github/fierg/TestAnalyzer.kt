package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.PeriodAnalyzer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.FileReader
import io.github.fierg.model.CompositionMode
import io.github.fierg.model.Options
import io.github.fierg.model.PlotType
import org.junit.Ignore
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
        PeriodAnalyzer.savePlotToFile("test-edge-plot.png", PeriodAnalyzer.createPlotFromOccurrences(plot))
    }

    @Test
    fun testAnalyzerGraph(){
        val f2fGraph = FileReader().getF2FNetwork(6)
        val decomposition = Decomposer(state = false, coroutines = true, clean = true, mode = CompositionMode.SIMPLE, deltaWindowAlgo = 0, skipSingleStepEdges = true)
        val decompositionResult = decomposition.findComposite(f2fGraph)


        val plot = PeriodAnalyzer.analyzeGraph(decompositionResult)
        PeriodAnalyzer.savePlotToFile("test-graph-plot.png", PeriodAnalyzer.createPlotFromOccurrences(plot, PlotType.GEOM_HIST))
    }


    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun testAnalyzerAllGraphs(){
        val options = Options.emptyOptions()
        options.dotenv = true
        DotEnvParser.readDotEnv(options)
        options.state = !options.state
        val evalResult = PeriodAnalyzer.analyzeAllGraphs(Decomposer(options))
        PeriodAnalyzer.savePlotToFile("test-all-graphs-plot1.png", PeriodAnalyzer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_HIST))
        PeriodAnalyzer.savePlotToFile("test-all-graphs-plot2.png", PeriodAnalyzer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_POINT))
        PeriodAnalyzer.savePlotToFile("test-all-covered-values.png", PeriodAnalyzer.createPieChartOfOccurrences(evalResult))
    }
}