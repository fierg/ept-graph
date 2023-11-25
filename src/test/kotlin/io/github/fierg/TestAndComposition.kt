package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestAndComposition {

    private val options = Options.emptyOptions()

    init {
        options.state = false
        options.skipSelfEdges = true
        options.compositionMode = CompositionMode.AND
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
    }

    @Test
    fun testDecompositionAND() {
        Logger.setLogLevelToDebug()
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)

        val decomposition = Decomposer(options)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(cover)

    }
}