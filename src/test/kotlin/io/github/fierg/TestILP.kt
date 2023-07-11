package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.model.CompositionMode
import io.github.fierg.model.Options
import org.junit.Ignore
import org.junit.Test

@Ignore
class TestILP {

    private val options = Options.emptyOptions()

    init {
        options.state = true
        options.coroutines = true
        options.clean = true
        options.mode = CompositionMode.SET_COVER_ILP
        options.skipSingleStepEdges = true
    }

    @Test
    fun testILP(){
        val f2fGraph = F2FReader().getF2FNetwork(0)
        val decomposition = Decomposer(options)

        decomposition.findComposite(f2fGraph)
    }
}