package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.factors
import io.github.fierg.extensions.maximalDivisors
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestPaperExamples {

    private val options = Options.emptyOptions()
    init {
        options.state = false
        options.skipSelfEdges = true
        options.compositionMode = CompositionMode.CLEAN_QUOTIENTS
    }

    @Test
    fun testExample1() {
        val number = 5
        val factors = number.factors().toList()
        Logger.info("Factors of $number are $factors")
        Logger.info("Max divisors are ${number.maximalDivisors().toList()}")
    }

    @Test
    fun testExample1cleanQuotients(){
        val array = arrayOf(false, true, false, true, false).toBooleanArray()
        val cover = Decomposer(options).findCover(array)
    }
}