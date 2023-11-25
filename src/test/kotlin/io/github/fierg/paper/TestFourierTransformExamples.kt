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

        Logger.info("Target: ${array.getBinaryString()} Factors: ${cover.factors}")
        cover.factors.forEach {factor ->
            Logger.info("Factor: ${factor.array.getBinaryString()}, rel size: ${factor.getRelativeSize(cover)} outliers: ${factor.outliers}")
            Logger.info("Combined outliers: ${factor.getOutliersOfCoverUntilThisFactor(cover)}")
            Logger.info("Combined covered values: ${factor.getCoveredValuesUntilThisFactor(cover)}")
            Logger.info("Combined relative covered values: ${factor.getRelativeCoveredValues(cover)}\n")
        }

        expectedFactors.forEach { target ->
            assert( resultingFactors.any { it.contentEquals(target) })
        }
    }
}