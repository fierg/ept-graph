package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode

class ChartGenerator {

    fun generatePlots() {
        Logger.info("Analyzing all graphs in all modes")
        Logger.info("This might take a while...")
        Logger.setLogLevelToDebug()
        val options = DotEnvParser.readDotEnv()

        CompositionMode.values().forEach { compositionMode ->
            DecompositionMode.values().forEach { decompositionMode ->
                Logger.info("Analyzing all graphs, reading options from .env file, using decomposition mode ${decompositionMode.name} and composition mode ${compositionMode.name}")
                Logger.setLogLevelToQuiet()
                val upTo = 61

                options.decompositionMode = decompositionMode
                options.compositionMode = compositionMode
                val decomposer = Decomposer(options)
                val evalResult = decomposer.analyzeAllGraphs(upTo)
                Logger.setLogLevelToDebug()

                val factorPlot1 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten())
                Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-size.png", factorPlot1)

                val factorPlot2 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten())
                Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum.png", factorPlot2)

                val factorPlot3 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), normalized = true)
                Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-normalized.png", factorPlot3)

                val factorPlot4 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), byFactorNr = true)
                Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-by-factor-nr.png", factorPlot4)
            }
            Logger.info("Done.")
        }
    }
}

fun main() {
    ChartGenerator().generatePlots()
}