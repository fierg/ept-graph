package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.reversed
import io.github.fierg.logger.Logger
import io.github.fierg.model.Defaults.Companion.DEFAULT_HEIGHT
import io.github.fierg.model.Defaults.Companion.DEFAULT_WIDTH
import io.github.fierg.model.Defaults.Companion.defaultPieCharConfig
import io.github.fierg.model.Defaults.Companion.defaultStyle
import io.github.fierg.model.EvaluationResult
import io.github.fierg.model.PlotType
import io.github.fierg.model.Style
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.annotations.layerLabels
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.tooltips.tooltipsNone
import java.awt.Desktop
import java.io.File


class Visualizer {

    companion object {

        private val lengthMapping = mapOf(
            0..16 to 0, // 2^1 to 2^4
            17..128 to 1, // 2^4 + 1 to 2^7
            129..1024 to 2,
            1025..2048 to 3,
            2049..5000 to 4,
            5001..Int.MAX_VALUE to 5
        )
        private val rlm = lengthMapping.reversed()

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

        fun createPlotFromOccurrences(result: Map<Int, Int>, type: PlotType = PlotType.GEOM_POINT, title: String = ""): Plot {
            val sortedResult = result.toSortedMap()
            val data = mapOf<String, Any>(
                "period Length" to sortedResult.keys.toList(),
                "occurrence" to sortedResult.values.toList()
            )
            Logger.debug("PLOT DATA: $data")

            return when (type) {
                PlotType.GEOM_POINT -> letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle(title) + geomPoint(size = 2.0) { x = "period Length"; y = "occurrence" }
                PlotType.GEOM_BAR -> letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle(title) + geomBar(stat = Stat.identity) { x = "period Length"; y = "occurrence" }
            }
        }

        fun createPieChartOfOccurrences(result: EvaluationResult, style: Style = defaultStyle, title: String = "Values covered by periods", showLegend: Boolean = true, width: Int = DEFAULT_WIDTH): Plot {
            val sortedMap = result.covers.toSortedMap()
            val data = mapOf<String, List<Int>>(
                "period length" to sortedMap.keys.toList(),
                "covered values" to sortedMap.values.toList()
            )

            val mappedData = mapOf<String, MutableList<Any>>(
                "name" to mutableListOf(
                    "ideal (up to ${rlm[0]!!.last})",
                    "very short (up to ${rlm[1]!!.last})",
                    "short (up to ${rlm[2]!!.last})",
                    " 3-5x multiple (up to ${rlm[3]!!.last})",
                    "2-3x multiple (up to ${rlm[4]!!.last})",
                    "outlier"
                ),
                "value" to mutableListOf(0, 0, 0, 0, 0, 0)
            )

            data["period length"]!!.forEach { length ->
                val index = lengthMapping.filter { it.key.contains(length) }.values.first()
                mappedData["value"]!![index] = mappedData["value"]!![index] as Int + data["covered values"]!![index]
            }

            Logger.debug("PLOT DATA: $mappedData")

            return when (style) {
                Style.PERCENT_AND_NAME -> letsPlot(mappedData) + defaultPieCharConfig + ggtitle(title) + ggsize(width, DEFAULT_HEIGHT) +
                            geomPie(size = 20, stroke = 1.0, tooltips = tooltipsNone, showLegend = showLegend,
                                labels = layerLabels().line("@name").line("(@{..prop..})").format("..prop..", ".0%").size(15))
                            { fill = "name"; weight = "value"; slice = "value" }

                Style.PERCENT -> letsPlot(mappedData) + defaultPieCharConfig + ggtitle(title) + ggsize(width, DEFAULT_HEIGHT) +
                            geomPie(hole = 0.2, size = 20, stroke = 1.0, tooltips = tooltipsNone, showLegend = showLegend,
                                labels = layerLabels("..proppct..").format("..proppct..", "{.1f}%").size(15))
                            { fill = "name"; weight = "value"; slice = "value" }

            }
        }

        private fun openPlotAsFile(content: String) {
            val dir = File(System.getProperty("user.dir"), "plots")
            dir.mkdir()
            val file = File(dir.canonicalPath, "temp-plot.html")
            file.createNewFile()
            file.writeText(content)
            Desktop.getDesktop().browse(file.toURI())
        }

        fun savePlotToFile(filename: String, plot: Plot, path: String = "./plots") {
            ggsave(plot, filename, path = path)
        }
        fun savePlotToFile(filename: String, plot: GGBunch, path: String = "./plots") {
            ggsave(plot, filename, path = path)
        }

        fun showPlotAsFile(plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            openPlotAsFile(content)
        }

        fun analyzeAllGraphs(decomposer: Decomposer, upTo: Int = 61): EvaluationResult {
            val factors = mutableMapOf<Int, Int>()
            val covers = mutableMapOf<Int, Int>()
            for (i in 0..upTo) {
                val f2fGraph = F2FReader().getF2FNetwork(i)
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
