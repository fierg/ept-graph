package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.factorsSequence

class Decomposition {

    val STATE = true

    fun findComposite(array: BooleanArray): Set<Pair<Int, Int>> {
        val periods = getPeriods(array)
        val cover = BooleanArray(array.size) {!STATE}
        val appliedPeriods = mutableSetOf<Pair<Int,Int>>()

        periods.forEach { period ->
            applyPeriod(cover,period)
            appliedPeriods.add(period)

            if ((array + cover).contentEquals(array)){
                return appliedPeriods
            }
        }
        throw NoCoverFoundException("No Cover found.")
    }

    private fun applyPeriod(cover: BooleanArray, period: Pair<Int, Int>) {
        var position = period.first
        while (position < cover.size) {
            cover[position] = STATE
            position += period.second
        }
        return
    }

    private fun getPeriods(array: BooleanArray): MutableSet<Pair<Int, Int>> {
        val factors = array.size.factorsSequence()
        val periods = mutableSetOf<Pair<Int, Int>>()
        factors.forEach { factor ->
            array.forEachIndexed { index, p ->
                if (p == STATE && isPeriodic(array, index, factor)) {
                    periods.add(Pair(index % factor, factor))
                }
            }
        }
        return periods
    }

    private fun isPeriodic(array: BooleanArray, index: Int, factor: Int): Boolean {
        val stepsToCheck = array.size / factor
        for (i in 0..stepsToCheck) {
            if (array[(index + i * factor) % array.size] != STATE) return false
        }
        return true
    }
}