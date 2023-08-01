package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.DotEnvParser
import io.github.fierg.data.F2FReader
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Factor
import org.junit.Test

class TestDecomposition {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.skipSelfEdges = true
    }

    @Test
    fun testDecomposition1() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(cover)

        assert(cover.outliers.size <= 3)
    }

    @Test
    fun testDecomposition1b() {
        val f2fGraph = F2FReader().getF2FNetwork(6)
        val edge = f2fGraph.edges.elementAt(1)
        val decomposition = Decomposer(state = true, threshold = 0.75)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(cover)
    }

    @Test
    fun testDecomposition3() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(options)
        decomposition.findComposite(f2fGraph)
    }

    @Test
    fun testDecomposition4() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(state = true, threshold = 0.8)
        decomposition.findComposite(f2fGraph)
    }

    @Test
    fun testDecomposition5() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(state = true, threshold = 0.6)
        decomposition.findComposite(f2fGraph)
    }


    @Test
    fun testDecomposition10() {
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(10))
    }

    @Test
    fun testDecomposition11() {
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(12))
    }

    @Test
    fun testDecomposition12() {
        val options = DotEnvParser.readDotEnv()
        val decomposition = Decomposer(options)
        decomposition.findComposite(F2FReader().getF2FNetwork(14))
    }


    @Test
    fun testDecompositionShortestPeriods() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = false, mode = CompositionMode.SHORTEST_PERIODS, threshold = 0.8)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)
        decomposition.analyzeCover(cover)

        assert(cover.outliers.size <= 6)
    }

    @Test
    fun testDecompositionMaxDivisors() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val edge = f2fGraph.edges.elementAt(6)
        val decomposition = Decomposer(state = true, mode = CompositionMode.MAX_DIVISORS)
        val cover = decomposition.findCover(f2fGraph.steps[edge]!!)

        assert(cover.getPrecision() < 1.0)
    }

    @Test
    fun testDecomposition3x() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val state = false
        val periods = Decomposer(state = state, threshold = 0.5).findCover(array)
        val expectedFactors = mutableListOf(Factor(arrayOf(false), listOf(0, 3, 4)), Factor(arrayOf(true, false, false), listOf(4)))
        val expectedPeriods = Cover(array, !state, 3, 3, mutableListOf(4), expectedFactors)

        assert(periods == expectedPeriods)
    }

    @Test
    fun testDecomposition3y() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val state = false
        val periods = Decomposer(state = state).findCover(array)
        val expectedFactors = mutableListOf(
            Factor(arrayOf(false), listOf(0, 3, 4)),
            Factor(arrayOf(true, false, false), listOf(4)),
            Factor(arrayOf(true, false, false, true, true, false), listOf())
        )
        val expectedPeriods = Cover(array, !state, 3, 6, mutableListOf(), expectedFactors)

        assert(periods == expectedPeriods)
    }

    @Test
    fun testDecomposition4a() {
        val input = BooleanArray(16) { true }
        val cover = Decomposer(state = false).findCover(input)
        val expectedFactors = mutableListOf(Factor(arrayOf(true), listOf()))

        assert(cover == Cover(input, true, 16, 1, mutableListOf(), expectedFactors))
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