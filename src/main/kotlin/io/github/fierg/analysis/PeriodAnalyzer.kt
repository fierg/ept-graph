package io.github.fierg.analysis

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.FileReader
import io.github.fierg.logger.Logger
import io.github.fierg.model.PlotType
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomHistogram
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import java.awt.Desktop
import java.io.File


class PeriodAnalyzer {

    companion object {
        fun analyzeGraph(decomposition: Collection<Collection<Pair<Int, Int>>>): MutableMap<Int, Int> {
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
                "factor" to result.toSortedMap().keys.toList(),
                "occurrence" to result.toSortedMap().values.toList()
            )
            Logger.info("PLOT DATA: $data")

            return when (type) {
                PlotType.GEOM_POINT -> letsPlot(data) + geomPoint(size = 2.0) { x = "factor"; y = "occurrence" }
                PlotType.GEOM_HIST -> letsPlot(data) + geomHistogram { x = "factor"; y = "occurrence" }
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
            ggsave(plot, filename, path = "./plots")
        }

        fun showPlot(plot: Plot) {
            val content = PlotSvgExport.buildSvgImageFromRawSpecs(plot.toSpec())
            openInBrowser(content)
        }

        fun analyzeAllGraphs(decomposer: Decomposer, type: PlotType = PlotType.GEOM_HIST) {
            val factors = mutableMapOf<Int,Int>()
            for (i in 0..61) {
                val f2fGraph = FileReader().getF2FNetwork(i)
                val decompositionResult = decomposer.findComposite(f2fGraph)

                val newFactors = analyzeGraph(decompositionResult)
                newFactors.forEach { (factor, occurrence) ->
                    factors[factor] = if (factors[factor] == null) occurrence else factors[factor]!! + occurrence
                }
            }
            saveToFile("test-all-graphs-plot.png", createPlot(factors, type))
        }
    }
}
