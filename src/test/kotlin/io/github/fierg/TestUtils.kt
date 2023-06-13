package io.github.fierg

import io.github.fierg.algo.Decomposition
import io.github.fierg.extensions.factorsSequence
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
        val expectedFactors = listOf(1,2,3,4,6,8,12,24)

        assert(factors == expectedFactors)
    }

    @Test
    fun testUtils3() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposition().findCover(array)
        val expectedPeriods = mutableSetOf(Pair(0,3),Pair(0,6),Pair(3,6),Pair(4,6))

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils4() {
        val cover = Decomposition(state = false).findCover(BooleanArray(16))

        assert(cover.contains(Pair(0,1)))
    }


}