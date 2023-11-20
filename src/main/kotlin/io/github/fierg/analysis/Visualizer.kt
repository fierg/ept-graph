package io.github.fierg.analysis

import io.github.fierg.extensions.reversed
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import io.github.fierg.model.style.DefaultPlotStyle.Companion.DEFAULT_HEIGHT
import io.github.fierg.model.style.DefaultPlotStyle.Companion.DEFAULT_WIDTH
import io.github.fierg.model.style.DefaultPlotStyle.Companion.defaultPieCharConfig
import io.github.fierg.model.style.DefaultPlotStyle.Companion.defaultStyle
import io.github.fierg.model.style.PieChartStyle
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.annotations.layerLabels
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomSmooth
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleYContinuous
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

        private fun openPlotAsFile(content: String) {
            val dir = File(System.getProperty("user.dir"), "plots")
            dir.mkdir()
            val file = File(dir.canonicalPath, "temp-plot.html")
            file.createNewFile()
            file.writeText(content)
            Desktop.getDesktop().browse(file.toURI())
        }

        fun savePlotToFile(filename: String, plot: Plot, path: String = "./plots") {
            Logger.debug("Saving file $filename to $path.")
            ggsave(plot, filename, path = path)
        }

        fun savePlotToFile(filename: String, plot: GGBunch, path: String = "./plots") {
            Logger.debug("Saving file $filename to $path.")
            ggsave(plot, filename, path = path)
        }

        fun showPlotAsFile(plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            openPlotAsFile(content)
        }

        private fun getPeriodLengthsFromResult(result: List<List<Cover>>): List<Int> {
            val periodLengths = mutableSetOf<Int>()
            result.flatten().forEach { cover ->
                cover.factors.forEach { factor -> periodLengths.add(factor.array.size) }
            }
            return periodLengths.toList()
        }

        fun createPieChartOfOccurrences(result: List<List<Cover>>, options: Options, style: PieChartStyle = defaultStyle, showLegend: Boolean = true, width: Int = DEFAULT_WIDTH): Plot {
            val data = mapOf(
                "period length" to getPeriodLengthsFromResult(result),
                "covered values" to emptyList()
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
            val basePlot = letsPlot(mappedData) + defaultPieCharConfig + ggtitle("Values covered by periods (state: ${!options.state})") + ggsize(width, DEFAULT_HEIGHT)

            return when (style) {
                PieChartStyle.PERCENT_AND_NAME -> basePlot + geomPie(
                    size = 20, stroke = 1.0, tooltips = tooltipsNone, showLegend = showLegend,
                    labels = layerLabels().line("@name").line("(@{..prop..})").format("..prop..", ".0%").size(15)
                )
                { fill = "name"; weight = "value"; slice = "value" }

                PieChartStyle.PERCENT -> basePlot + geomPie(
                    hole = 0.2, size = 20, stroke = 1.0, tooltips = tooltipsNone, showLegend = showLegend,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}%").size(15)
                )
                { fill = "name"; weight = "value"; slice = "value" }

            }
        }

        fun createCoverByFactorPlot(covers: List<Cover>): Plot {
            val xS = "Rel Factor Size"
            val yS = "Sum of covered values"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, Pair<Int, Int>>()
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size] == null) {
                        resultMap[size] = Pair(cover.totalValues - factor.outliers.size, cover.totalValues)
                    } else {
                        resultMap[size] = Pair(resultMap[size]!!.first + cover.totalValues - factor.outliers.size, resultMap[size]!!.second + cover.totalValues)

                    }
                }
            }

            return generatePointPlot(resultMap, xS, yS)
        }

        fun createCoverByFactorPlotNormalized(covers: List<Cover>, useAverage: Boolean = false): Plot {
            val xS = "Rel Factor Size"
            val yS = "Rel amount of values covered"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, MutableList<Double>>()
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size].isNullOrEmpty()) {
                        resultMap[size] = mutableListOf(factor.getRelativeCoveredValues(cover))
                    } else {
                        resultMap[size]!!.add(factor.getRelativeCoveredValues(cover))
                    }
                }
            }

            return generatePointPlotForNormalized(resultMap, xS, yS, useAverage)
        }

        fun createCoverByDecompositionPlot(covers: List<Cover>, useAverage: Boolean = false): Plot {
            val xS = "Rel Cover Size"
            val yS = "Rel amount of values covered"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, MutableList<Double>>()
            covers.forEach { cover ->
                val size = cover.size.toDouble() / cover.target.size
                if (resultMap[size].isNullOrEmpty()) {
                    resultMap[size] = mutableListOf(cover.getRelativeCoveredValues())
                } else {
                    resultMap[size]!!.add(cover.getRelativeCoveredValues())
                }
            }

            return generatePointPlotForNormalized(resultMap, xS, yS, useAverage)
        }

        private fun generatePointPlotForNormalized(resultMap: Map<Double, List<Double>>, xS: String, yS: String, useAverage: Boolean): Plot {
            val data = if (useAverage) {
                mapOf(
                    xS to resultMap.keys.toList(),
                    yS to resultMap.values.map { it.average() }
                )
            } else {
                val expandedResultList = expandToResultList(resultMap)
                mapOf(
                    xS to expandedResultList.map { it.first },
                    yS to expandedResultList.map { it.second }
                )
            }

            Logger.info("Generating plot...")
            Logger.info("PLOT DATA: $data")
            return letsPlot(data) +
                    ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                    ggtitle("% of Values covered by factor of size x") +
                    //scaleYContinuous(limits = Pair(0,1)) +
                    geomPoint(size = 2.0) { x = xS; y = yS }
            //geomBoxplot { x = xS; y = yS }  +
            //geomSmooth(se = FALSE, method = "gam") +
        }

        private fun generatePointPlot(resultMap: Map<Double, Pair<Int, Int>>, xS: String, yS: String): Plot {
            val sortedMap = resultMap.toSortedMap()
            val maxValue = sortedMap.values.fold(0) { acc, pair -> acc + pair.second }
            val sumList = sortedMap.values.runningReduce { acc, pair -> Pair(pair.first + acc.first, maxValue) }.toMutableList()
            sumList[0] = Pair(sumList[0].first, maxValue)
            6
            val data = mapOf(
                xS to sortedMap.keys.toList(),
                yS to sumList.map { it.first }
            )
            Logger.info("Generating plot...")
            Logger.info("PLOT DATA: $data")
            return letsPlot(data) +
                    ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                    ggtitle("Sum of Values covered by factor of size x") +
                    scaleYContinuous(limits = Pair(0, sumList.last().first)) +
                    geomPoint(size = 2.0) { x = xS; y = yS } +
                    geomSmooth(method = "loess")
        }

        private fun expandToResultList(resultMap: Map<Double, List<Double>>): List<Pair<Double, Double>> {
            val result = mutableListOf<Pair<Double, Double>>()
            resultMap.entries.forEach { entry ->
                entry.value.forEach { value ->
                    result.add(Pair(entry.key, value))
                }
            }
            return result
        }

        fun createCoverByFactorPlotNormalizedByCover(covers: List<Cover>): Plot {
            val xS = "Rel Factor Size"
            val yS = "Sum of covered values"
            Logger.info("Collecting values to plot...")
            val resultMap = mutableMapOf<Double, Int>()
            var totalValuesToCover = 0
            covers.forEach { cover ->
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    if (resultMap[size] == null) resultMap[size] = 0
                }
            }

            covers.forEach { cover ->
                totalValuesToCover += cover.totalValues
                cover.factors.forEach { factor ->
                    val size = factor.getRelativeSize(cover)
                    val value = factor.getCoveredValuesUntilThisFactor(cover)
                    resultMap.keys.filter { it >= size }.forEach { size ->
                        resultMap[size] = resultMap[size]!! + value
                    }
                }
            }

            return generatePointPlotNormalizedByCover(resultMap, totalValuesToCover, xS, yS)
        }

        private fun generatePointPlotNormalizedByCover(resultMap: MutableMap<Double, Int>, totalValuesToCover: Int, xS: String, yS: String): Plot {
            val sortedMap = resultMap.toSortedMap()
            val data = mapOf(
                xS to sortedMap.keys.toList(),
                yS to sortedMap.values.toList()
            )
            Logger.info("Generating plot...")
            Logger.info("PLOT DATA: $data")
            return letsPlot(data) +
                    ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                    ggtitle("Sum of Values covered by factor of size x") +
                    //scaleYContinuous(limits = Pair(0, totalValuesToCover)) +
                    geomPoint(size = 2.0) { x = xS; y = yS } +
                    geomSmooth(method = "loess")
        }

    }
}
