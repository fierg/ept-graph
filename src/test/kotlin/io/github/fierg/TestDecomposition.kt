package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.DotEnvParser
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
        decomposition.analyze(f2fGraph.steps[edge]!!.size, cover)

        assert(cover.outliers.size <= 3)
    }

    @Test
    fun testDecomposition2(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true, threshold = 0.8)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyze(f2fGraph.steps[edge]!!.size, cover)

        assert(cover.outliers.size <= 6)
    }

    @Test
    fun testDecomposition3(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(options)
        decomposition.findComposite(f2fGraph)
    }

    @Test
    fun testDecomposition4(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(state = true, skipSingleStepEdges = true, threshold = 0.8)
        decomposition.findComposite(f2fGraph)
    }

    @Test
    fun testDecomposition5(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(state = true, skipSingleStepEdges = true, threshold = 0.6)
        decomposition.findComposite(f2fGraph)
    }


    @Test
    fun testDecomposition10(){
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(10))
    }

    @Test
    fun testDecomposition11(){
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(12))
    }

    @Test
    fun testDecomposition12(){
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(14))
    }

}