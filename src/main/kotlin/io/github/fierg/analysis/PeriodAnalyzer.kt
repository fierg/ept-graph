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
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.letsPlot
import java.awt.Desktop
import java.io.File


class PeriodAnalyzer {
    companion object {
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
                "factor" to result.toSortedMap().keys.toList(),
                "occurrence" to result.toSortedMap().values.toList()
            )
            Logger.info("PLOT DATA: $data")

            return when (type) {
                PlotType.GEOM_POINT -> letsPlot(data) + geomPoint(size = 2.0) { x = "Period Length"; y = "occurrence" }
                PlotType.GEOM_HIST -> letsPlot(data) + geomHistogram { x = "Period Length"; y = "occurrence" }
            }
        }

        fun createPieChartOfOccurrences(result: EvaluationResult): Plot {
            val data = mapOf<String, Any>(
                "period length" to result.covers.toSortedMap().keys.toList(),
                "covered values" to result.covers.toSortedMap().values.toList()
            )

            Logger.info("PLOT DATA: $data")

            return letsPlot(data) +
                    geomPie(stat = Stat.identity, stroke = 1, strokeColor = "white", hole = 0.5)
                    { slice = "value"; fill = "name" }
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
