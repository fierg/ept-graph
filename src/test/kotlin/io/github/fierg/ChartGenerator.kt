package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.model.Defaults
import io.github.fierg.model.PlotType
import org.jetbrains.letsPlot.GGBunch
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.junit.Ignore
import org.junit.Test

@Ignore
class ChartGenerator {

    @Test
    @Ignore //Ignored in default test suit because of long run time of around 2-3 minutes
    fun testAnalyzerAllGraphs(){
        val upTo = 61
        val options = DotEnvParser.readDotEnv()
        var evalResult = Visualizer.analyzeAllGraphs(Decomposer(options),1)
        val barPlot1 = Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_BAR, "All Periods of all Graphs (state: ${!options.state}, mode: ${options.mode})")
        val geomPlot1 = Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_POINT, "All Periods of all Graphs (state: ${!options.state}, mode: ${options.mode})")
        val piePlot1 = Visualizer.createPieChartOfOccurrences(evalResult, title = "Values covered by periods (state: ${!options.state}, mode: ${options.mode})", showLegend = false, width = 400)
        options.state = !options.state
        evalResult = Visualizer.analyzeAllGraphs(Decomposer(options),1)
        val barPlot2 = Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_BAR, "All Periods of all Graphs (state: ${!options.state}, mode: ${options.mode})")
        val geomPlot2 = Visualizer.createPlotFromOccurrences(evalResult.factors, PlotType.GEOM_POINT, "All Periods of all Graphs (state: ${!options.state}, mode: ${options.mode})")
        val piePlot2 = Visualizer.createPieChartOfOccurrences(evalResult, title = "Values covered by periods (state: ${!options.state}, mode: ${options.mode})")

        val combinedBar = GGBunch().addPlot(barPlot1, 0, 0).addPlot(barPlot2, 0, Defaults.DEFAULT_HEIGHT)
        val combinedPoint = GGBunch().addPlot(geomPlot1, 0, 0).addPlot(geomPlot2, 0, Defaults.DEFAULT_HEIGHT)
        val combinedPie = GGBunch().addPlot(piePlot1, 0, 0).addPlot(piePlot2, Defaults.DEFAULT_WIDTH, 0)

        Visualizer.savePlotToFile("all-graphs-bar-char-combined.png", combinedBar)
        Visualizer.savePlotToFile("all-graphs-point-plot-combined.png", combinedPoint)
        Visualizer.savePlotToFile("all-covered-values-pie-chart-combined.png", combinedPie)
    }
}