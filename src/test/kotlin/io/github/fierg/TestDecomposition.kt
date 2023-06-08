package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.data.FileReader
import io.github.fierg.model.CompositionMode
import org.junit.Test

class TestDecomposition {

    @Test
    fun testDecomposition(){
        val f2fGraph = FileReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposition(mode = CompositionMode.SIMPLE, clean = true, coroutines = true, state = false)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val trivialPeriods = periods.count { it.second == f2fGraph.steps[edge]!!.size }
        assert(trivialPeriods <= 3)
    }
}