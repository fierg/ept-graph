package io.github.fierg.ONESvsZEROS

import io.github.fierg.algo.Decomposer
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class OnesVSZerosTest {
    init {
        Logger.setLogLevelToDebug()
    }

    @Test
    fun testOrExample0() {
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val options = Options.emptyOptions()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        options.threshold = 1.0
        options.compositionMode = CompositionMode.OR
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
    fun testAndExample0() {
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