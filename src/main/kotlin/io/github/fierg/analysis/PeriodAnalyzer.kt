package io.github.fierg.analysis

import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import java.awt.Desktop
import java.io.File


class PeriodAnalyzer {
    companion object {
        fun analyzePeriods(periods: Collection<Pair<Int, Int>>) : Plot {
            val factorMap = mutableMapOf<Int, Int>()

            periods.forEach { period ->
                factorMap[period.second] = if (factorMap[period.second] == null) 1 else factorMap[period.second]!! + 1
            }

            return createPlot(factorMap)
        }

        private fun createPlot(result: Map<Int, Int>): Plot {
            val data = mapOf<String, Any>(
                "occurrence" to result.toSortedMap().values.toList(),
                "factor" to result.toSortedMap().keys.toList()
            )

            return letsPlot(data) + geomPoint(data) { x = "factor"; y = "occurrence" }
        }

        fun openInBrowser(content: String) {
            val dir = File(System.getProperty("user.dir"), "plots")
            dir.mkdir()
            val file = File(dir.canonicalPath, "temp-plot.html")
            file.createNewFile()
            file.writeText(content)
            Desktop.getDesktop().browse(file.toURI())
        }

        fun saveToFile(filename: String, plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            val dir = File(System.getProperty("user.dir"), "plots")
            dir.mkdir()
            val file = File(dir.canonicalPath, filename)
            file.createNewFile()
            file.writeText(content)
        }

        fun showPlot(plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            openInBrowser(content)
        }
    }
}
