package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.data.F2FReader
import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.maximalDivisors
import io.github.fierg.extensions.primeFactors
import io.github.fierg.model.result.Decomposition
import io.github.fierg.periodic.Periodic
import org.junit.Test

class TestUtils {

    @Test
    fun testUtils1() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val period = Periodic().findShortestPeriod(array)
        println("period is $period")

        assert(period == 6)
    }

    @Test
    fun testUtils2() {
        val number = 24
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
        val expectedFactors = listOf(1, 2, 3, 4, 6, 8, 12, 24)

        assert(factors == expectedFactors)
    }

    @Test
    fun testUtils22() {
        val number = 28
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
    }

    @Test
    fun testUtils3() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false, threshold = 0.5).findCover(array)
        val expectedPeriods = Decomposition(3,3, listOf(4), listOf(true,false,false).toBooleanArray())

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils3b() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(state = false).findCover(array)
        val expectedPeriods = Decomposition(3,6, emptyList(), listOf(true,false,false,true,true,false).toBooleanArray())

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils4() {
        val cover = Decomposer(state = false).findCover(BooleanArray(16) {true})
        assert(cover == Decomposition(16,1, emptyList(), BooleanArray(1){true}))
    }

    @Test
    fun testUtils10(){
        val b1 = BooleanArray(8) {false}
        val b2 = BooleanArray(2) {false}
        b2[1] = true
        val result = BooleanArray(8)
        result[1] = true
        result[3] = true
        result[5] = true
        result[7] = true


        b1.applyPeriod(b2, true)
        assert(b1.contentEquals(result))
    }

    @Test
    fun testUtils11() {
        val f2fGraph = F2FReader().getF2FNetwork(0)
        f2fGraph.getGraphAtStep(2)
        f2fGraph.getGraphAtStep(4)
        f2fGraph.getGraphAtStep(6)
    }

    @Test
    fun testPrimefactors(){
        val number = 7260
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
        println("Prime factors are ${number.primeFactors().toList()}")
    }

    @Test
    fun testMaximalDivisors(){
        val number = 7260
        val factors = number.factorsSequence().toList()
        println("Factors of $number are $factors")
        println("Maximal Divisors are ${number.maximalDivisors().toList()}")
    }

}