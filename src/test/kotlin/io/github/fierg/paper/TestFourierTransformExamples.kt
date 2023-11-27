package io.github.fierg.paper

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.getBinaryString
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestFourierTransformExamples {

    private val options = Options.emptyOptions()

    init {
        Logger.setLogLevelToDebug()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
    }

    @Test
    fun testExample1cleanFactor1() {
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1cleanFactor2() {
        val array = arrayOf(false, true, false, true, true, true).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testCleanFactor1() {
        val array = arrayOf(true, false, true, true, true, false, true, false, true, false).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }


    @Test
    fun testExample1cleanQuotients1b(){
        val array = arrayOf(false, true, false, true, true, true).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
        options.threshold = 1.0
        options.compositionMode = CompositionMode.OR
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false,true).toBooleanArray(), arrayOf(false,true,false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert( resultingFactors.any { it.contentEquals(target) })
        }
    }


    @Test
    fun testFourierAndExample(){
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
        options.threshold = 1.0
        options.compositionMode = CompositionMode.AND
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false,true).toBooleanArray(), arrayOf(true,true,false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert( resultingFactors.any { it.contentEquals(target) })
        }
    }

    @Test
    fun testFourierOrExample(){
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.FOURIER_TRANSFORM
        options.threshold = 1.0
        options.compositionMode = CompositionMode.OR
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false, true, false, false, false, false).toBooleanArray(), arrayOf(false, false, false, true, false, false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert( resultingFactors.any { it.contentEquals(target) })
        }
    }
}