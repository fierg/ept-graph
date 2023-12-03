package io.github.fierg.deltaWindow

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.getBinaryString
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestDeltaWindowDecomposition {

    val options = Options.emptyOptions()

    init {
        Logger.setLogLevelToDebug()
        options.skipSelfEdges = true
        options.decompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS
        options.threshold = 1.0
        options.deltaWindowAlgo = 1
    }

    @Test
    fun testIsPeriodicDeltaWindow() {
        val array = arrayOf(false, true, false, true, true, true, false, true, false, true).toBooleanArray()
        options.compositionMode = CompositionMode.OR
        val d = Decomposer(options)
        val index = 1
        val factorSize = 2
        Logger.info("is ${array.getBinaryString()} at index $index with factorSize $factorSize periodic: ${d.isPeriodic(array, index, factorSize)}")
    }

    @Test
    fun testOrExample() {
        val array = arrayOf(false, true, false, true, true, true, false, true, false, true).toBooleanArray()
        options.compositionMode = CompositionMode.OR
        val d = Decomposer(options)
        val index = 1
        val factorSize = 2
        Logger.info("is ${array.getBinaryString()} at place $index with factorSize $factorSize periodic: ${d.isPeriodic(array, index, factorSize)}")
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(false, true).toBooleanArray())

        expectedFactors.forEach { target ->
            assert(resultingFactors.any { it.contentEquals(target) })
        }
    }

    @Test
    fun testAndExample() {
        val array = arrayOf(true, false, true, false, false, false, true, false, true, false).toBooleanArray()
        options.compositionMode = CompositionMode.AND
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
        val resultingFactors = cover.factors.map { it.array }
        val expectedFactors = listOf(arrayOf(true, false).toBooleanArray())

        expectedFactors.forEach { target ->
            assert(resultingFactors.any { it.contentEquals(target) })
        }
    }
}