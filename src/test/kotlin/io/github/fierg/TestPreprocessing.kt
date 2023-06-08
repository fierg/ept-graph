package io.github.fierg

import io.github.fierg.algo.Preprocessing
import io.github.fierg.data.FileReader
import org.junit.Test

class TestPreprocessing {

    @Test
    fun testPreprocessingWidth1() {
        val input = booleanArrayOf(false, false, true, false, true, false, false, false, true)
        val expectedOutput = booleanArrayOf(false, true, true, true, true, true, false, true, true)
        val width = 1
        val output = Preprocessing.applyDeltaWindow(input, width)
        println("Input: " + input.contentToString())
        println("Output: " + output.contentToString())

        assert(output.contentEquals(expectedOutput))
    }

    @Test
    fun testPreprocessingWidth2() {
        val input = booleanArrayOf(false, false, true, false, true, false, false, false, true)
        val expectedOutput = booleanArrayOf(true, true, true, true, true, true, true, true, true)
        val width = 2
        val output = Preprocessing.applyDeltaWindow(input, width)
        println("Input: " + input.contentToString())
        println("Output: " + output.contentToString())

        assert(output.contentEquals(expectedOutput))
    }

    @Test
    fun testPreprocessingGraph() {
        val width = 2
        val f2fGraph = FileReader().getF2FNetwork(0)
        Preprocessing.applyDeltaWindow(f2fGraph,width)
    }

}