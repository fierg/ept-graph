package io.github.fierg.chart

import io.github.fierg.analysis.Visualizer
import io.github.fierg.logger.Logger
import io.github.fierg.model.style.DefaultPlotStyle.Companion.DEFAULT_HEIGHT
import io.github.fierg.model.style.DefaultPlotStyle.Companion.DEFAULT_WIDTH
import io.github.fierg.model.style.DefaultPlotStyle.Companion.blankTheme
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.annotations.layerLabels
import org.jetbrains.letsPlot.asDiscrete
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillBrewer
import org.jetbrains.letsPlot.scale.scaleFillManual
import org.jetbrains.letsPlot.tooltips.tooltipsNone
import org.junit.Test

class TestCharts {
    private val data = mapOf(
        "name" to listOf("ideal (up to 16)", "very short (up to 128)", "short (up to 1024)", " 3-5x multiple (up to 2048)", "2-3x multiple (up to 5000)", "outlier"),
        "value" to listOf(4882, 32967, 21312, 6586, 729, 13098)
    )

    private val data2 = mapOf("name" to listOf("a", "b", "c", "d", "b"), "value" to listOf(40, 90, 10, 50, 20))

    @Test
    fun testPieChart() {
        Logger.debug("Test generation of pie chart 1 ...")
        val plot = letsPlot(data2) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Pie Chart") +
                blankTheme +
                geomPie(
                    size = 15, hole = 0.2,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}").size(15)
                ) { fill = asDiscrete("value"); weight = "value" }

        Visualizer.savePlotToFile("test-pie-chart.png", plot, "test-plots")
    }

    @Test
    fun testPieChart2() {
        Logger.debug("Test generation of pie chart 2 ...")
        val plot = letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Pie Chart") +
                blankTheme +
                geomPie(
                    size = 25, hole = 0.3,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}").size(15)
                ) { fill = asDiscrete("name"); weight = "value" }

        Visualizer.savePlotToFile("test-pie-chart-real-world-data.png", plot, "test-plots")
    }

    @Test
    fun testPieChart3(){
        Logger.debug("Test generation of pie chart 3 ...")
        val plot = letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Pie Chart") +
                blankTheme +
                geomPie(stat = Stat.identity, hole = 0.3, tooltips = tooltipsNone, size = 25)
                { slice = "value"; fill = "name" } +
                scaleFillBrewer(palette = "Set1")

        Visualizer.savePlotToFile("test-pie-chart-old.png", plot, "test-plots")
    }

    @Test
    fun testPieChartPPCT(){
        Logger.debug("Test generation of ppct chart ...")
        val plot = letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Pie Chart") +
                blankTheme +
                geomPie(hole = 0.2, size = 20, stroke = 1.0, tooltips = tooltipsNone,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}%").size(15))
                { fill = "name"; weight = "value"; slice = "value" } +
                scaleFillBrewer(palette = "Set1")

        Visualizer.savePlotToFile("test-pie-chart-PPCT.png", plot, "test-plots")
    }

    @Test
    fun testPieChartPPCT2(){
        Logger.debug("Test generation ppct pie chart 2 ...")
        val plot = letsPlot(data2) + scaleFillManual(values = listOf("#61BAFF", "#04FF00", "#91FF00","#d4FF00", "#FF9500", "#FF0000")) +
                ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Pie Chart") +
                blankTheme +
                geomPie(size = 20, stroke = 1.0, tooltips = tooltipsNone, showLegend = false,
                    labels = layerLabels().line("@name").line("(@{..prop..})").format("..prop..", ".0%").size(10))
                { fill = "name"; weight = "value"; slice = "value"}

        Visualizer.savePlotToFile("test.png", plot, "test-plots")
    }


    @Test
    fun testHistogramChart(){
        Logger.debug("Test generation of histogram and plots ...")
        val plot1 = letsPlot(data2) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Point Chart") + geomPoint(size = 2.0) { x = "name"; y = "value" }
        val plot2 = letsPlot(data2) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) + ggtitle("Test Histogram Chart") + geomHistogram { x = "name"; y = "value" }

        Visualizer.savePlotToFile("test-geom-point.png", plot1, "test-plots")
        Visualizer.savePlotToFile("test-histo.png", plot2, "test-plots")

    }
}