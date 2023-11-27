package io.github.fierg.paper

import io.github.fierg.algo.Decomposer
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestPaperGreedyShortFactorsExample {

    private val options = Options.emptyOptions()

    init {
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        Logger.setLogLevelToDebug()
    }

    @Test
    fun testExample1ShortFactor1() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1MaxDivisors1() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        options.decompositionMode = DecompositionMode.MAX_DIVISORS
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1MaxDivisors2() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        options.compositionMode = CompositionMode.AND
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1Fourier() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        options.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }
}