package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.model.options.Options
import org.junit.Test

class IntegrationTest {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.skipSingleStepEdges = true
        options.deltaWindowAlgo = 0
    }

    @Test
    fun testIntegration(){
        val array = booleanArrayOf(true, false, false, true, true, false)
        val decomposition = Decomposer(options)

        val result = decomposition.findCover(array)
        decomposition.analyzeCover(array.size, result)
    }
}