package io.github.fierg.chart

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import org.junit.Ignore
import org.junit.Test

@Ignore
class ChartGenerator {

    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun plotValuesCoveredByFactors() {
        Logger.info("Analyzing all graphs, reading options from .env file")
        Logger.setLogLevelToQuiet()
        val upTo = 20
        val options = DotEnvParser.readDotEnv()
        val decomposer = Decomposer(options)
        val evalResult = decomposer.analyzeAllGraphs(upTo)
        Logger.setLogLevelToDebug()

        val factorPlot1 = Visualizer.createCoverByFactorPlot(evalResult.flatten())
        Visualizer.savePlotToFile("all-relative-values-by-factor-size.png", factorPlot1)

        val coverPlot1 = Visualizer.createCoverByDecompositionPlot(evalResult.flatten())
        Visualizer.savePlotToFile("all-relative-values-by-cover-size.png", coverPlot1)

        val factorPlot2 = Visualizer.createCoverByFactorPlot(evalResult.flatten(), useAverage = true)
        Visualizer.savePlotToFile("all-relative-values-by-factor-size-average.png", factorPlot2)

        val coverPlot2 = Visualizer.createCoverByDecompositionPlot(evalResult.flatten(), useAverage = true)
        Visualizer.savePlotToFile("all-relative-values-by-cover-size-average.png", coverPlot2)

        Logger.info("Done.")
    }
}