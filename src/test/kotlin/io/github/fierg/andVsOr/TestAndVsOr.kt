package io.github.fierg.andVsOr

import io.github.fierg.algo.Decomposer
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestAndVsOr {
    init {
        Logger.setLogLevelToDebug()
    }

    @Test
    fun testOrExample() {
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        options.threshold = 1.0
        options.compositionMode = CompositionMode.OR
        options.allowFullLengthDecomposition = true
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false, true, false, true, false, false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert(resultingFactors.any { it.contentEquals(target) })
        }
    }

    @Test
    fun testAndExample() {
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        options.threshold = 1.0
        options.compositionMode = CompositionMode.AND
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false, true).toBooleanArray(), arrayOf(true, true, false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert(resultingFactors.any { it.contentEquals(target) })
        }
    }
}