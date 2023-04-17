package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.minus
import io.github.fierg.extensions.plus
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger

class Decomposition {

    companion object {
        val STATE = true
    }

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

            if (array.contentEquals(cover)) {
                return appliedPeriods
            }
        }

        var coverage = cover.count { it == STATE }.toDouble() / array.count { it == STATE }
        if (coverage.isNaN()) coverage = 0.0

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
        val periods = mutableSetOf<Pair<Int, Int>>()
        array.indices.forEach { factor ->
            array.forEachIndexed { index, p ->
                if (index < factor) {
                    if (p == STATE && isPeriodic(array, index, factor)) {
                        periods.add(Pair(index, factor))
                    }
                }
            }
        }
        return periods
    }

    private fun isPeriodic(array: BooleanArray, index: Int, factor: Int): Boolean {
        var pos = (index + factor) % array.size
        while (pos != index) {
            if (array[pos] != STATE) return false
            pos = (pos + factor) % array.size
        }
        return true
    }
}