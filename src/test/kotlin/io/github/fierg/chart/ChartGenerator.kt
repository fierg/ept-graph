package io.github.fierg.chart

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import org.junit.Ignore
import org.junit.Test

@Ignore
class ChartGenerator {

    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun generatePlots() {
        Logger.info("Analyzing all graphs in all modes")
        Logger.info("This might take a while...")
        CompositionMode.values().forEach { compositionMode ->
            Logger.info("Analyzing all graphs, reading options from .env file, using composition mode ${compositionMode.name}")
            Logger.setLogLevelToQuiet()
            val upTo = 61
            val options = DotEnvParser.readDotEnv()
            options.compositionMode = compositionMode
            val decomposer = Decomposer(options)
            val evalResult = decomposer.analyzeAllGraphs(upTo)
            Logger.setLogLevelToDebug()

            val factorPlot1 = Visualizer.createCoverByFactorPlot(evalResult.flatten())
            Visualizer.savePlotToFile("${options.compositionMode.name}-all-relative-values-by-factor-size.png", factorPlot1)

            val coverPlot1 = Visualizer.createCoverByDecompositionPlot(evalResult.flatten())
            Visualizer.savePlotToFile("${options.compositionMode.name}-all-relative-values-by-cover-size.png", coverPlot1)

            val factorPlot2 = Visualizer.createCoverByFactorPlot(evalResult.flatten(), useAverage = true)
            Visualizer.savePlotToFile("${options.compositionMode.name}-all-relative-values-by-factor-size-average.png", factorPlot2)

            val coverPlot2 = Visualizer.createCoverByDecompositionPlot(evalResult.flatten(), useAverage = true)
            Visualizer.savePlotToFile("${options.compositionMode.name}-all-relative-values-by-cover-size-average.png", coverPlot2)
        }
        Logger.info("Done.")
    }
}