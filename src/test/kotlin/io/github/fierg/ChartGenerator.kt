package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.logger.Logger
import io.github.fierg.model.style.Defaults.Companion.DEFAULT_HEIGHT
import io.github.fierg.model.style.PlotType
import org.jetbrains.letsPlot.GGBunch
import org.junit.Ignore
import org.junit.Test

@Ignore
class ChartGenerator {

    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun testAnalyzerAllGraphs() {
        val upTo = 61
        val options = DotEnvParser.readDotEnv()
        options.state = !options.state
        var evalResult = Visualizer.analyzeAllGraphs(Decomposer(options), upTo)

        val barPlot1 = Visualizer.createPlotFromOccurrences(evalResult, options, PlotType.GEOM_BAR)
        val geomPlot1 = Visualizer.createPlotFromOccurrences(evalResult, options, PlotType.GEOM_POINT)
        val piePlot1 = Visualizer.createPieChartOfOccurrences(evalResult, options, showLegend = false)

        Logger.info("In total, ${evalResult.totalValues} values have been covered with a total of ${evalResult.totalPeriods} periods.")

        options.state = !options.state
        evalResult = Visualizer.analyzeAllGraphs(Decomposer(options), upTo)

        val barPlot2 = Visualizer.createPlotFromOccurrences(evalResult, options, PlotType.GEOM_BAR)
        val geomPlot2 = Visualizer.createPlotFromOccurrences(evalResult, options, PlotType.GEOM_POINT)
        val piePlot2 = Visualizer.createPieChartOfOccurrences(evalResult, options)

        val combinedBar = GGBunch().addPlot(barPlot1, 0, 0).addPlot(barPlot2, 0, DEFAULT_HEIGHT)
        val combinedPoint = GGBunch().addPlot(geomPlot1, 0, 0).addPlot(geomPlot2, 0, DEFAULT_HEIGHT)
        val combinedPie = GGBunch().addPlot(piePlot1, 0, 0).addPlot(piePlot2, 0, DEFAULT_HEIGHT)

        Visualizer.savePlotToFile("all-graphs-bar-char-combined.png", combinedBar)
        Visualizer.savePlotToFile("all-graphs-point-plot-combined.png", combinedPoint)
        Visualizer.savePlotToFile("all-covered-values-pie-chart-combined.png", combinedPie)

        Logger.info("In total, ${evalResult.totalValues} values have been covered with a total of ${evalResult.totalPeriods} periods.")
    }
}