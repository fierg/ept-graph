package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.minus
import io.github.fierg.extensions.plus
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger

class Decomposition {

    val STATE = true

    fun findComposite(graph: EPTGraph) {
        graph.edges.forEach { edge ->
            try {
                Decomposition().findCover(graph.steps[edge]!!)
                Logger.info("Found decomposition.")
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }
    }

    fun findCover(array: BooleanArray): Set<Pair<Int, Int>> {
        val periods = getPeriods(array)
        val cover = BooleanArray(array.size) { !STATE }
        val appliedPeriods = mutableSetOf<Pair<Int, Int>>()

        periods.forEach { period ->
            applyPeriod(cover, period)
            appliedPeriods.add(period)

            if ((array.plus(cover)).contentEquals(array)) {
                return appliedPeriods
            }
        }
        val coveredOnes = array.minus(cover).asIterable().count { it }
        val uncoveredOnes = array.asIterable().count { it }
        val coverage =
            if ((coveredOnes.toFloat() / (uncoveredOnes + coveredOnes)).isNaN()) 0
            else (coveredOnes.toFloat() / (uncoveredOnes + coveredOnes))
        throw NoCoverFoundException("with coverage of $coverage")
    }

    private fun applyPeriod(cover: BooleanArray, period: Pair<Int, Int>) {
        var position = period.first
        while (position < cover.size) {
            cover[position] = STATE
            position += period.second
        }
    }

    private fun getPeriods(array: BooleanArray): MutableSet<Pair<Int, Int>> {
        val factors = array.size.factorsSequence()
        val periods = mutableSetOf<Pair<Int, Int>>()
        val maxRange = array.size.toFloat() / 2
        factors.forEach { factor ->
            array.forEachIndexed { index, p ->
                if (index < maxRange) {
                    if (p == STATE && isPeriodic(array, index, factor)) {
                        periods.add(Pair(index % factor, factor))
                    }
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