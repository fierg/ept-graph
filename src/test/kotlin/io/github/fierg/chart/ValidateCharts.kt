package io.github.fierg.chart

import io.github.fierg.algo.Decomposer
import io.github.fierg.analysis.Visualizer
import io.github.fierg.extensions.getRandomArray
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.style.DefaultPlotStyle
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.letsPlot
import org.junit.Test

class ValidateCharts {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
    }

    @Test
    fun testRandomArrayPlotGeneration() {
        val size = 10000
        Logger.info("Generating random plot of size $size")
        val array = getRandomArray(size)

        val d = Decomposer(options)
        val cover = d.findCover(array)
        val resultMap = cover.getAbsoluteResultMapFromCover()

        val data = mapOf(
            "x" to resultMap.map { it.key },
            "y" to resultMap.map { it.value }
        )

        Logger.info("Generating plot...")
        Logger.info("PLOT DATA: $data")
        val plot = letsPlot(data) +
                ggsize(DefaultPlotStyle.DEFAULT_WIDTH, DefaultPlotStyle.DEFAULT_HEIGHT) +
                ggtitle("% of Values covered by factor of size x") +
                //scaleYContinuous(limits = Pair(0, 1)) +
        geomPoint(size = 2.0) { x = "x"; y = "y" }

        Visualizer.savePlotToFile("test-random.png", plot, "test-plots")
    }
}