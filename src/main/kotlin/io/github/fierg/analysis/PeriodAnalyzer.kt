package io.github.fierg.analysis

import io.github.fierg.logger.Logger
import io.github.fierg.model.PlotType
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.geom.geomHistogram
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import java.awt.Desktop
import java.io.File


class PeriodAnalyzer {

    companion object {
        fun analyzeGraph(decomposition: Collection<Collection<Pair<Int, Int>>>): MutableMap<Int,Int> {
            val factorMap = mutableMapOf<Int, Int>()

            decomposition.forEach { periods ->
                periods.forEach { period ->
                    factorMap[period.second] = if (factorMap[period.second] == null) 1 else factorMap[period.second]!! + 1
                }
            }

            return factorMap
        }

        fun analyzePeriods(periods: Collection<Pair<Int, Int>>): MutableMap<Int, Int> {
            val factorMap = mutableMapOf<Int, Int>()

            periods.forEach { period ->
                factorMap[period.second] = if (factorMap[period.second] == null) 1 else factorMap[period.second]!! + 1
            }

            return factorMap
        }

        fun createPlot(result: Map<Int, Int>, type: PlotType = PlotType.GEOM_POINT): Plot {
            val data = mapOf<String, Any>(
                "occurrence" to result.toSortedMap().values.toList(),
                "factor" to result.toSortedMap().keys.toList()
            )
            Logger.info("PLOT DATA: $data")

            return when(type){
                PlotType.GEOM_POINT -> letsPlot(data) + geomPoint(data) { x = "factor"; y = "occurrence" }
                PlotType.GEOM_HIST -> letsPlot(data) + geomHistogram(data) { x = "factor"; y = "occurrence" }
            }
        }

        private fun openInBrowser(content: String) {
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
