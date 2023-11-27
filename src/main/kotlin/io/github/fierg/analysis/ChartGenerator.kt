package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import jetbrains.letsPlot.Geom
import jetbrains.letsPlot.stat.statBin

class ChartGenerator {

    fun generatePlots() {
        Logger.info("Analyzing all graphs in all modes")
        Logger.info("This might take a while...")
        Logger.setLogLevelToDebug()
        val options = DotEnvParser.readDotEnv()

        CompositionMode.values().forEach { compositionMode ->
            DecompositionMode.values().forEach { decompositionMode ->
                if (compositionMode == CompositionMode.AND && decompositionMode == DecompositionMode.FOURIER_TRANSFORM) {
                    Logger.info("Skipping unsupported mode")
                } else {
                    Logger.info("Analyzing all graphs, reading options from .env file, using decomposition mode ${decompositionMode.name} and composition mode ${compositionMode.name}")

                    val upTo = 61
                    options.decompositionMode = decompositionMode
                    options.compositionMode = compositionMode

                    val decomposer = Decomposer(options)

                    Logger.setLogLevelToQuiet()
                    val evalResult = decomposer.analyzeAllGraphs(upTo)
                    Logger.resetLogLevel()

                    val boxPlot1 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, showOutliers = false)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot.png", boxPlot1, "./plots/box-plots")

                    val boxPlot2 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, showOutliers = true)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-outliers.png", boxPlot2, "./plots/box-plots")

                    val boxPlot3 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, minDistance = 0.1)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-dist.png", boxPlot3, "./plots/box-plots")

                    val boxPlot4 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, useFactorNrInsteadOfSize = true)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-factor-nr.png", boxPlot4, "./plots/box-plots")

                    val factorPlot1 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten())
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-size.png", factorPlot1, "./plots/point-plots")

                    val factorPlot2 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten())
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum.png", factorPlot2, "./plots/point-plots")

                    val factorPlot3 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), normalized = true)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-normalized.png", factorPlot3, "./plots/point-plots")

                    val factorPlot4 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), byFactorNr = true)
                    Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-by-factor-nr.png", factorPlot4, "./plots/point-plots")


                }
            }
            Logger.info("Done.")
        }
    }
}

fun main() {
    ChartGenerator().generatePlots()
}