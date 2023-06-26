package io.github.fierg

import io.github.fierg.analysis.PeriodAnalyzer
import io.github.fierg.model.Defaults.Companion.DEFAULT_HEIGHT
import io.github.fierg.model.Defaults.Companion.DEFAULT_WIDTH
import io.github.fierg.model.Defaults.Companion.blankTheme
import jetbrains.letsPlot.Geom
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.annotations.layerLabels
import org.jetbrains.letsPlot.asDiscrete
import org.jetbrains.letsPlot.geom.geomPie
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillBrewer
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
        val plot = letsPlot(data2) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                blankTheme +
                geomPie(
                    size = 15, hole = 0.2,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}").size(15)
                ) { fill = asDiscrete("value"); weight = "value" }

        PeriodAnalyzer.savePlotToFile("test-pie-chart.png", plot, "test")
    }

    @Test
    fun testPieChart2() {
        val plot = letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                blankTheme +
                geomPie(
                    size = 25, hole = 0.3,
                    labels = layerLabels("..proppct..").format("..proppct..", "{.1f}").size(15)
                ) { fill = asDiscrete("name"); weight = "value" }

        PeriodAnalyzer.savePlotToFile("test-pie-chart-real-world-data.png", plot, "test")
    }

    @Test
    fun testPieChart3(){
        val plot = letsPlot(data) + ggsize(DEFAULT_WIDTH, DEFAULT_HEIGHT) +
                blankTheme +
                geomPie(stat = Stat.identity, hole = 0.3, tooltips = tooltipsNone, size = 25)
                { slice = "value"; fill = "name" } +
                scaleFillBrewer(palette = "Set1")

        PeriodAnalyzer.savePlotToFile("test-pie-chart-old.png", plot, "test")

    }
}