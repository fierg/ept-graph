package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.F2FReader
import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Factor
import org.junit.Test
import kotlin.test.assertFailsWith

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
        decomposition.analyzeCover(f2fGraph.steps[edge]!!.size, cover)

        assert(cover.outliers.size <= 3)
    }

    @Test
    fun testDecomposition1b(){
        val f2fGraph = F2FReader().getF2FNetwork(6)
        val edge = f2fGraph.edges.elementAt(1)
        val decomposition = Decomposer(state = true, threshold = 0.75)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(f2fGraph.steps[edge]!!.size, cover)
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


    @Test
    fun testDecompositionShortestPeriods(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false, threshold = 0.8, mode = CompositionMode.SHORTEST_PERIODS)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(f2fGraph.steps[edge]!!.size, cover)

        assert(cover.outliers.size <= 6)
    }

    @Test
    fun testDecompositionMaxDivisors(){
        assertFailsWith<NoCoverFoundException> {
            val f2fGraph = F2FReader().getF2FNetwork(0)
            val edge = f2fGraph.edges.elementAt(6)
            val decomposition = Decomposer(state = false, threshold = 0.8, mode = CompositionMode.MAX_DIVISORS)
            val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        }
    }

    @Test
    fun testUtils3() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false, threshold = 0.5).findCover(array)
        val expectedFactors = listOf(Factor(arrayOf(false), listOf(0,3,4)), Factor(arrayOf(true,false,false), listOf(4)))
        val expectedPeriods = Cover(3,3, listOf(4), listOf(true,false,false).toBooleanArray(), expectedFactors)
        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils3b() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false).findCover(array)
        val expectedFactors = listOf(
            Factor(arrayOf(false), listOf(0,3,4)),
            Factor(arrayOf(true,false,false), listOf(4)),
            Factor(arrayOf(true,false,false,true,true,false), listOf())
        )
        val expectedPeriods = Cover(3,6, emptyList(), listOf(true,false,false,true,true,false).toBooleanArray(),expectedFactors)

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils4() {
        val cover = Decomposer(state = false).findCover(BooleanArray(16) {true})
        val expectedFactors = listOf(Factor(arrayOf(true), listOf()))
        assert(cover == Cover(16,1, emptyList(), BooleanArray(1){true}, expectedFactors))
    }

    @Test
    fun getOutliersFalse() {
        val array = arrayOf(true, false, false, true, false, false).toBooleanArray()
        val cover = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val outliers = Decomposer(state = true).getOutliers(array, cover = cover)
        val expectedOutliers = listOf(4)

        assert(outliers == expectedOutliers)
    }

    @Test
    fun getOutliersTrue() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val cover = arrayOf(true, false, false, true, false, false).toBooleanArray()
        val outliers = Decomposer(state = false).getOutliers(array, cover = cover)
        val expectedOutliers = listOf(4)

        assert(outliers == expectedOutliers)
    }
}