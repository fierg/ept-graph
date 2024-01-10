package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.format
import io.github.fierg.extensions.median
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class ChartGenerator {

    @ExperimentalTime
    fun decomposeAllAndGeneratePlotsAndTables() {
        Logger.info("Analyzing all graphs in all modes")
        Logger.info("This might take a while...")
        Logger.setLogLevelToDebug()

        Options.getDefaultSuit().forEach { options ->
            val decomposer = Decomposer(options)

            Logger.setLogLevelToQuiet()
            val (evalResult, elapsed) = measureTimedValue {
                decomposer.analyzeAllGraphs()
            }
            Logger.resetLogLevel()
            Logger.info("This took $elapsed.")

            generatePlots(evalResult, options)
            generateMetricTables(evalResult, options)
        }
        Logger.info("Done.")
    }

    private fun generateMetricTables(evalResult: List<List<Cover>>, options: Options) {
        var foundValidDecomposition = 0
        var hardOutliers = 0
        var veryGoodDecomposition = 0
        var okDecomposition = 0
        val mDecompositionStructures = mutableListOf<Double>()
        var mPrecision = mutableListOf<Double>()
        val mSize = mutableListOf<Int>()
        evalResult.flatten().forEach { cover ->
            val coverArray = cover.getCoverArray()
            if (cover.target.contentEquals(coverArray)) {
                foundValidDecomposition++
            }
            cover.target.forEachIndexed { index, b ->
                if (coverArray[index] != b) hardOutliers++
            }
            val precision = cover.getPrecision()
            if (precision > 0.9) {
                veryGoodDecomposition++
                okDecomposition++
            } else if (precision > 0.75) {
                okDecomposition++
            }
            mDecompositionStructures.add(cover.getModifiedDecompositionStructure())
            mPrecision.add(precision)
            mSize.add(cover.factors.size)
        }
        mPrecision = mPrecision.filter { !it.isNaN() }.filter { it != 0.0 }.toMutableList()
        println("valid decompositions: $foundValidDecomposition, good decompositions: $veryGoodDecomposition, ok decompositions:$okDecomposition total hard outliers $hardOutliers")
        //println("& ${mDecompositionStructures.sum().format(3)} & ${mDecompositionStructures.average().format(3)} & ${mDecompositionStructures.median().format(3)} &" +
        //        " ${mPrecision.sum().format(3)} & ${mPrecision.average().format(3)} & ${mPrecision.median().format(3)} &" +
        //        " ${mSize.sum()} & ${mSize.average().format(3)} & ${mSize.median()}")

        println(" ${mPrecision.average().format(3)} & ${mPrecision.median().format(3)} ")
    }

    private fun generatePlots(evalResult: List<List<Cover>>, options: Options) {
        val folder = "./plots/"
        val boxPlot1 =
            Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, showOutliers = false, xS = "Relative Factor Size", yS = "Relative covered values in cover")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot.png", boxPlot1, "${folder}box-plots")

        val boxPlot2 =
            Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, showOutliers = true, xS = "Relative Factor Size", yS = "Relative covered values in cover")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-outliers.png", boxPlot2, "${folder}box-plots")

        val minDistance = 0.05
        val boxPlot3 =
            Analyzer.createCoverByFactorPlotNormalized(
                evalResult.flatten(),
                createBoxPlot = true,
                minDistance = minDistance,
                showOutliers = true,
                xS = "Relative Factor Size (min distance $minDistance)",
                yS = "Relative covered values in cover"
            )
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-dist.png", boxPlot3, "${folder}box-plots")

        val boxPlot4 =
            Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), createBoxPlot = true, useFactorNrInsteadOfSize = true, xS = "Factor Percentile", yS = "Relative covered values in cover")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-boxplot-factor-nr.png", boxPlot4, "${folder}box-plots")

        val factorPlot1 = Analyzer.createCoverByFactorPlotNormalized(evalResult.flatten(), xS = "Relative Factor Size", yS = "Relative covered values")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-relative-values-by-factor-size.png", factorPlot1, "${folder}point-plots")

        val factorPlot2 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), xS = "Relative Factor Size", yS = "Sum of Covered Values")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum.png", factorPlot2, "${folder}point-plots")

        val factorPlot3 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), normalized = true, xS = "Relative Factor Size", yS = "% of Sum of Covered Values")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-normalized.png", factorPlot3, "${folder}point-plots")

        val factorPlot3b = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), normalized = true, fitCurve = true, xS = "Relative Factor Size", yS = "% of Sum of Covered Values")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-normalized-curve.png", factorPlot3b, "${folder}point-plots")

        val factorPlot4 = Analyzer.createCoverByFactorPlotSum(evalResult.flatten(), byFactorNr = true, xS = "Factor Nr", yS = "Sum of Covered Values")
        Visualizer.savePlotToFile("${options.decompositionMode.name}-${options.compositionMode.name}-all-values-by-factor-sum-by-factor-nr.png", factorPlot4, "${folder}point-plots")
    }
}

@OptIn(ExperimentalTime::class)
fun main() {
    ChartGenerator().decomposeAllAndGeneratePlotsAndTables()
}