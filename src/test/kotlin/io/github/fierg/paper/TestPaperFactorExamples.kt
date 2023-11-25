package io.github.fierg.paper

import io.github.fierg.algo.Decomposer
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestPaperFactorExamples {

    private val options = Options.emptyOptions()
    init {
        options.state = false
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.MAX_DIVISORS
    }

    @Test
    fun testExample1cleanFactor1(){
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }
    @Test
    fun testExample1cleanFactor2(){
        val array = arrayOf(false, true, false, true, true, true).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }
}