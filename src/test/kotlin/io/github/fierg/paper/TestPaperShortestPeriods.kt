package io.github.fierg.paper

import io.github.fierg.algo.Decomposer
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestPaperShortestPeriods {

    private val options = Options.emptyOptions()

    init {
        options.state = false
        options.skipSelfEdges = true
        options.compositionMode = CompositionMode.SHORTEST_PERIODS
    }

    @Test
    fun testExample1ShortFactor1() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1MaxDivisors() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        options.compositionMode = CompositionMode.MAX_DIVISORS
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1Fourier() {
        val array = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray()
        options.compositionMode = CompositionMode.FOURIER_TRANSFORM
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }
}