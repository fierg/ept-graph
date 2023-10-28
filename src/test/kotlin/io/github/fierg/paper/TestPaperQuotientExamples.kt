package io.github.fierg.paper

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.factors
import io.github.fierg.extensions.maximalDivisors
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import org.junit.Test

class TestPaperQuotientExamples {

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
        Logger.info("Max divisors of $number are ${number.maximalDivisors().toList()}")
    }

    @Test
    fun testExample2() {
        val number = 6
        val factors = number.factors().toList()
        Logger.info("Factors of $number are $factors")
        Logger.info("Max divisors of $number are ${number.maximalDivisors().toList()}")
    }


    @Test
    fun testExample1cleanQuotients1(){
        val array = arrayOf(false, true, false, true, false, false).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }

    @Test
    fun testExample1cleanQuotients2(){
        val array = arrayOf(false, true, false, true, true, true).toBooleanArray()
        val d = Decomposer(options)
        val cover = d.findCover(array)
        d.analyzeCover(cover)
    }
}