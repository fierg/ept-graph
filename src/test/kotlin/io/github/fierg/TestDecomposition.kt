package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.model.options.CompositionMode
import org.junit.Test

class TestDecomposition {

    @Test
    fun testDecomposition1(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false, coroutines = true, clean = true, mode = CompositionMode.SIMPLE)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val trivialPeriods = periods.count { it.second == f2fGraph.steps[edge]!!.size }
        assert(trivialPeriods <= 3)
    }

    @Test
    fun testDecomposition2(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true, coroutines = true, clean = true, mode = CompositionMode.SIMPLE)
        val periods = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, periods)

        val trivialPeriods = periods.count { it.second == f2fGraph.steps[edge]!!.size }
        assert(trivialPeriods <= 3)
    }

    @Test
    fun testPeriodAggregator(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true, coroutines = true, clean = true, mode = CompositionMode.AGGREGATOR)
        decomposition.findCover(f2fGraph.steps[edge]!!)

    }
}