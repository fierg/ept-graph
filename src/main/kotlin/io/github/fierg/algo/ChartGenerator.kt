package io.github.fierg.algo

import io.github.fierg.analysis.Analyzer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.DecompositionMode

class ChartGenerator {

    fun generatePlots() {
        Logger.info("Analyzing all graphs in all modes")
        Logger.info("This might take a while...")
        DecompositionMode.values()
            .forEach { compositionMode ->
                Logger.info("Analyzing all graphs, reading options from .env file, using composition mode ${compositionMode.name}")
                Logger.setLogLevelToQuiet()
                val upTo = 61
                val options = DotEnvParser.readDotEnv()
                options.decompositionMode = compositionMode
                val decomposer = Decomposer(options)
                val evalResult = decomposer.analyzeAllGraphs(upTo)
                Logger.setLogLevelToDebug()

                val factorPlot1 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten())
                Visualizer.savePlotToFile("${options.decompositionMode.name}-all-relative-values-by-factor-size.png", factorPlot1)

                val factorPlot = Analyzer.createCoverByFactorPlot(evalResult.flatten())
                Visualizer.savePlotToFile("${options.decompositionMode.name}-all-values-by-factor-size.png", factorPlot)

                val factorPlot2 = Analyzer.createCoverByFactorPlotNormalizedByCover(evalResult.flatten())
                Visualizer.savePlotToFile("${options.decompositionMode.name}-all-values-by-factor-size-normalized-by-cover.png", factorPlot2)
            }
        Logger.info("Done.")
    }
}

fun main() {
    ChartGenerator().generatePlots()
}