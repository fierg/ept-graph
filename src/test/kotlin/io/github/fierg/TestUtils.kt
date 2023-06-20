package io.github.fierg

import io.github.fierg.algo.Decomposer
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.model.CompositionMode
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
    fun testUtils3() {
        val array = arrayOf(true, false, false, true, true, false).toBooleanArray()
        val periods = Decomposer(mode = CompositionMode.SIMPLE).findCover(array)
        val expectedPeriods = mutableSetOf(Triple(0, 3, 2), Triple(4, 6, 1))

        assert(periods == expectedPeriods)
    }

    @Test
    fun testUtils4() {
        val cover = Decomposer(state = false).findCover(BooleanArray(16))
        assert(cover.contains(Triple(0, 1, 16)))
    }
}