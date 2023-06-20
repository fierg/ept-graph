package io.github.fierg

import io.github.fierg.algo.Preprocessor
import io.github.fierg.data.FileReader
import io.github.fierg.extensions.contentEqualsWithDelta
import io.github.fierg.extensions.valueOfDeltaWindow
import org.junit.Test


class TestDeltaWindow {

    @Test
    fun testPreprocessingWidth1() {
        val input = booleanArrayOf(false, false, true, false, false, false, false, false, true)
        val expectedOutput = booleanArrayOf(false, false, true, true, false, false, false, false, true)
        val width = 1
        val output = Preprocessor.applyDeltaWindow(input, width, state = true)
        println("Input: \t\t\t" + input.contentToString())
        println("Output: \t\t" + output.contentToString())
        println("Expected output: \t" + expectedOutput.contentToString())

        assert(output.contentEquals(expectedOutput))
    }

    @Test
    fun testPreprocessingWidth2() {
        val input = booleanArrayOf(false, false, true, false, true, false, false, false, true)
        val expectedOutput = booleanArrayOf(false, false, true, true, true, true, true, false, true)
        val width = 2
        val output = Preprocessor.applyDeltaWindow(input, width, state = true)
        println("Input: \t\t\t" + input.contentToString())
        println("Output: \t\t" + output.contentToString())
        println("Expected output: \t" + expectedOutput.contentToString())

        assert(output.contentEquals(expectedOutput))
    }

    @Test
    fun testPreprocessingGraph() {
        val width = 2
        val f2fGraph = FileReader().getF2FNetwork(0)
        Preprocessor.applyDeltaWindow(f2fGraph, width, state = true)
    }

    @Test
    fun testDeltaWindowExtensionFunctionTrue() {
        val array1 = booleanArrayOf(false, false, true, false, true, false)
        val array2 = booleanArrayOf(false, false, true, false, true, true)

        val isEqual = array1.contentEqualsWithDelta(array2, 1, true)

        println("Array 1: " + array1.contentToString())
        println("Array 2: " + array2.contentToString())

        assert(isEqual)
    }

    @Test
    fun testDeltaWindowExtensionFunctionFalse() {
        val array1 = booleanArrayOf(false, false, true, false, true, false)
        val array2 = booleanArrayOf(false, true, true, false, false, false)

        val isEqual = array1.contentEqualsWithDelta(array2, 1, true)

        println("Array 1: " + array1.contentToString())
        println("Array 2: " + array2.contentToString())

        assert(!isEqual)
    }

    @Test
    fun testDeltaWindowValueFunction() {
        val array = booleanArrayOf(false, false, true, false, false, false)
        println("Array 1: " + array.contentToString())

        assert(array.valueOfDeltaWindow(1, 1, true))
        assert(array.valueOfDeltaWindow(1, 2, true))
        assert(!array.valueOfDeltaWindow(1, 3, true))
    }
}