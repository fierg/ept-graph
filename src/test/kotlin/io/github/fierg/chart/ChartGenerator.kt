package io.github.fierg.chart

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.style.DefaultPlotStyle.Companion.DEFAULT_HEIGHT
import org.jetbrains.letsPlot.GGBunch
import org.junit.Ignore
import org.junit.Test

@Ignore
class ChartGenerator {



    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun testAnalyzerAllGraphs() {
        Logger.info("Analyzing all graphs, reading options from .env file")

        val upTo = 61
        val options = DotEnvParser.readDotEnv()
        val decomposer = Decomposer(options)
        val evalResult = decomposer.analyzeAllGraphs(upTo)
        val plot = Visualizer.createCoverByFactorPlot(evalResult.flatten())


        Visualizer.savePlotToFile("all-covered-values-pie-chart-combined.png", plot)

        Logger.info("Done.")
    }





}