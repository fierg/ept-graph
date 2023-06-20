package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.FileReader
import io.github.fierg.logger.Logger
import io.github.fierg.model.EvaluationResult
import io.github.fierg.model.PlotType
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillBrewer
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.theme
import org.jetbrains.letsPlot.tooltips.tooltipsNone
import java.awt.Desktop
import java.io.File


class PeriodAnalyzer {
    companion object {
        val DEFAULT_WIDTH = 600
        val DEFAULT_HEIGHT = 375
        val lengthMapping = mapOf(
            0..16 to 0, // 2^0 to 2^4
            17..128 to 1, // 2^4 + 1 to 2^7
            129..1024 to 2,
            1025..2048 to 3,
            2049..5000 to 4,
            5001..Int.MAX_VALUE to 5
        )

        fun analyzeGraph(decomposition: Collection<Collection<Triple<Int, Int, Int>>>): MutableMap<Int, Int> {
            val factorMap = mutableMapOf<Int, Int>()
            decomposition.forEach { periods ->
                periods.forEach { period ->
                    factorMap[period.second] = if (factorMap[period.second] == null) 1 else factorMap[period.second]!! + 1
                }
            }

            return factorMap
        }

        fun analyzePeriods(periods: Collection<Triple<Int, Int, Int>>): MutableMap<Int, Int> {
            val factorMap = mutableMapOf<Int, Int>()

            periods.forEach { period ->
                factorMap[period.second] = if (factorMap[period.second] == null) 1 else factorMap[period.second]!! + 1
            }

            return factorMap
        }

        fun createPlotFromOccurrences(result: Map<Int, Int>, type: PlotType = PlotType.GEOM_POINT): Plot {
            val data = mapOf<String, Any>(
                "period Length" to result.toSortedMap().keys.toList(),
                "occurrence" to result.toSortedMap().values.toList()
            )
            Logger.info("PLOT DATA: $data")

            return when (type) {
                PlotType.GEOM_POINT -> letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + geomPoint(size = 2.0) { x = "period Length"; y = "occurrence" }
                PlotType.GEOM_HIST -> letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + geomHistogram { x = "period Length"; y = "occurrence" }
            }
        }

        fun createPieChartOfOccurrences(result: EvaluationResult): Plot {
            val data = mapOf<String, List<Int>>(
                "period length" to result.covers.toSortedMap().keys.toList(),
                "covered values" to result.covers.toSortedMap().values.toList()
            )

            val mappedData = mapOf<String, MutableList<Any>>(
                "group" to mutableListOf("ideal", "very short", "short", "multiple", "single multiple", "outlier"),
                "covered values" to mutableListOf(0, 0, 0, 0, 0, 0)
            )

            data["period length"]!!.forEachIndexed { i, length ->
                val index = lengthMapping.filter { it.key.contains(length) }.values.first()
                mappedData["covered values"]!![index] = mappedData["covered values"]!![index] as Int + data["covered values"]!![i]
            }

            Logger.info("PLOT DATA: $mappedData")

            return letsPlot(mappedData) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                    geomPie(stat = Stat.identity, hole = 0.3, tooltips = tooltipsNone)
                    { slice = "covered values"; fill = "group" } +
                    scaleFillBrewer(palette = "Set1")
        }

        private fun openPlotInBrowser(content: String) {
            val dir = File(System.getProperty("user.dir"), "plots")
            dir.mkdir()
            val file = File(dir.canonicalPath, "temp-plot.html")
            file.createNewFile()
            file.writeText(content)
            Desktop.getDesktop().browse(file.toURI())
        }

        fun savePlotToFile(filename: String, plot: Plot) {
            ggsave(plot, filename, path = "./plots")
        }

        fun showPlotInBrowser(plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            openPlotInBrowser(content)
        }

        fun analyzeAllGraphs(decomposer: Decomposer): EvaluationResult {
            val factors = mutableMapOf<Int, Int>()
            val covers = mutableMapOf<Int, Int>()
            for (i in 0..61) {
                val f2fGraph = FileReader().getF2FNetwork(i)
                val decompositionResult = decomposer.findComposite(f2fGraph)
                val newFactors = analyzeGraph(decompositionResult)
                newFactors.forEach { (factor, occurrence) ->
                    factors[factor] = if (factors[factor] == null) occurrence else factors[factor]!! + occurrence
                }
                decompositionResult.forEach { periods ->
                    periods.forEach { (_, factor, coverage) ->
                        covers[factor] = if (covers[factor] == null) coverage else covers[factor]!! + coverage
                    }
                }
            }
            return EvaluationResult(factors, covers)
        }
    }
}
