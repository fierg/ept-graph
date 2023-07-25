package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.model.options.Options
import org.junit.Test

class TestDecomposition {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.skipSingleStepEdges = true
    }

    @Test
    fun testDecomposition1(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, cover)

        assert(cover.outliers.size <= 3)
    }

    @Test
    fun testDecomposition2(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph, edge, cover)

        assert(cover.outliers.size <= 3)
    }

    @Test
    fun testDecomposition3(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(options)
        decomposition.findComposite(f2fGraph)
    }
}