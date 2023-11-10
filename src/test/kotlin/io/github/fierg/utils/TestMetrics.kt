package io.github.fierg.utils

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Options
import org.junit.Test

class TestMetrics {

    private val options = Options.emptyOptions()

    init {
        options.state = false
        options.skipSelfEdges = true
    }

    @Test
    fun testMetrics() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposer = Decomposer(options)
        val decomposition = decomposer.findComposite(f2fGraph)
        Logger.info("Cover has width ${decomposition.first().getWidth()}")
        Logger.info("Cover has periodicity ${decomposition.first().getPeriodicity()}")
        Logger.info("Cover has decomposition structure ${decomposition.first().getDecompositionStructure()}")

    }
}