package io.github.fierg.utils

import io.github.fierg.extensions.getBinaryString
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Factor
import org.junit.Test

class TestGetCoverArray {

    @Test
    fun testCoverArrayAND() {
        val compositionMode = CompositionMode.AND
        val factor = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray(),
            stateToReplace = false,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor),
            compositionMode = compositionMode,
            flippedState = false
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor: ${factor.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")

        assert(cover.getCoverArray().contentEquals(cover.target))
    }

    @Test
    fun testCoverArrayOR() {
        val compositionMode = CompositionMode.OR
        val factor = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray(),
            stateToReplace = true,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor),
            compositionMode = compositionMode,
            flippedState = false
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor: ${factor.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")

        assert(cover.getCoverArray().contentEquals(cover.target))
    }

    @Test
    fun testCoverArrayANDf() {
        val compositionMode = CompositionMode.AND
        val factor = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray(),
            stateToReplace = false,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor),
            compositionMode = compositionMode,
            flippedState = true
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor: ${factor.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")

        assert(cover.getCoverArray().contentEquals(cover.target))
    }

    @Test
    fun testCoverArrayORf() {
        val compositionMode = CompositionMode.OR
        val factor = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, false, true, false, true).toBooleanArray(),
            stateToReplace = true,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor),
            compositionMode = compositionMode,
            flippedState = true
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor: ${factor.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")


        assert(cover.getCoverArray().contentEquals(cover.target))
    }


    @Test
    fun testCoverArrayOR2() {
        val compositionMode = CompositionMode.OR
        val factor1 = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val factor2 = Factor(arrayOf(false,true,false).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, true, true).toBooleanArray(),
            stateToReplace = true,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor1, factor2),
            compositionMode = compositionMode,
            flippedState = false
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor1: ${factor1.array.getBinaryString()}")
        Logger.info("Factor2: ${factor2.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")


        assert(cover.getCoverArray().contentEquals(cover.target))
    }

    @Test
    fun testCoverArrayAND2() {
        val compositionMode = CompositionMode.AND
        val factor1 = Factor(arrayOf(false,true).toBooleanArray(), mutableListOf(),compositionMode)
        val factor2 = Factor(arrayOf(true,true,false).toBooleanArray(), mutableListOf(),compositionMode)
        val cover = Cover(
            target = arrayOf(false, true, false, true, false, false).toBooleanArray(),
            stateToReplace = false,
            totalValues = 4,
            size = 2,
            outliers = mutableListOf(),
            factors = mutableListOf(factor1, factor2),
            compositionMode = compositionMode,
            flippedState = true
        )

        Logger.info("Composition mode: ${compositionMode.name}")
        Logger.info("Factor1: ${factor1.array.getBinaryString()}")
        Logger.info("Factor2: ${factor2.array.getBinaryString()}")
        Logger.info("Target: ${cover.target.getBinaryString()}")
        Logger.info("Cover:  ${cover.getCoverArray().getBinaryString()}")


        assert(cover.getCoverArray().contentEquals(cover.target))
    }
}