package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
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

        private fun getPeriodLengthsFromResult(result: List<List<Cover>>): List<Int> {
            val periodLengths = mutableSetOf<Int>()
            result.flatten().forEach { cover ->
                cover.factors.forEach { factor -> periodLengths.add(factor.cover.size) }
            }
            return periodLengths.toList()
        }

        fun createPieChartOfOccurrences(result: List<List<Cover>>, options: Options, style: PieChartStyle = defaultStyle, showLegend: Boolean = true, width: Int = DEFAULT_WIDTH): Plot {
            val data = mapOf<String, List<Int>>(
                "period length" to getPeriodLengthsFromResult(result),
                "covered values" to emptyList<Int>()
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

        fun createCoverByFactorPlot(flatten: List<Cover>): Plot {
            TODO("Not yet implemented")
            //letsPlot(data2) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Point Chart") + geomPoint(size = 2.0) { x = "name"; y = "value" }
        }
    }
}
