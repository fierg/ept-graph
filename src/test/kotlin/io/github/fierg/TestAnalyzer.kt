package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.F2FReader
import io.github.fierg.model.options.Options
import io.github.fierg.model.style.PlotStyle
import org.junit.Test

class TestAnalyzer {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.skipSingleStepEdges = true
    }

    /*
    @Test
    fun testAnalyzerAllGraphs(){
        val options = DotEnvParser.readDotEnv()
        val evalResult = Visualizer.analyzeAllGraphs(Decomposer(options),1)
        Visualizer.savePlotToFile("test-all-covered-values-pie-chart.png", Visualizer.createPieChartOfOccurrences(evalResult, options), "./test-plots")
    }

     */
}