package io.github.fierg.analysis

import io.github.fierg.extensions.reversed
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
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

        /*
        fun createPieChartOfOccurrences(result: Decomposition, options: Options, style: PieChartStyle = defaultStyle, showLegend: Boolean = true, width: Int = DEFAULT_WIDTH): Plot {
            val sortedMap = result
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

         */

    }
}
