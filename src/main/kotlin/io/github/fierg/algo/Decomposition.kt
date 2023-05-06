package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.CompositionMode
import io.github.fierg.model.SelfAwareEdge
import kotlinx.coroutines.*

class Decomposition(private val state: Boolean = true, private val coroutines: Boolean = false, private val clean: Boolean = false, private val mode: CompositionMode = CompositionMode.ALL) {

    fun findComposite(graph: EPTGraph) {
        Logger.info("Looking for $state values while decomposing.")
        if (coroutines) Logger.info("Using coroutines to compute periods.")
        if (clean) Logger.info("Cleaning up multiples/duplicates before applying the periods.")
        Logger.info("Choosing periods in $mode mode.")

        graph.edges.forEach { edge ->
            try {
                val decomposition = findCover(graph.steps[edge]!!)
                analyze(graph, edge, decomposition)
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }
    }

    private fun analyze(graph: EPTGraph, edge: SelfAwareEdge, decomposition: Set<Pair<Int, Int>>) {
        val valuesToCover = graph.steps[edge]!!.count { it == state }
        val trivialPeriods = decomposition.count { it.second == graph.steps[edge]!!.size }

        Logger.info("Found decomposition with ${String.format("%5d", decomposition.size)} periods, " +
                    "covered ${String.format("%5d", valuesToCover)} values, " +
                    "used ${String.format("%3d", ((trivialPeriods.toFloat() / decomposition.size) * 100).toInt())}% trivial periods."
        )
    }


    fun findCover(array: BooleanArray): Set<Pair<Int, Int>> {
        val periods = cleanMultiplesOfIntervals(if (coroutines) getPeriodsCO(array) else getPeriods(array), clean)
        var cover = BooleanArray(array.size) { !state }
        val appliedPeriods = mutableSetOf<Pair<Int, Int>>()

        when (mode) {
            CompositionMode.ALL -> {
                periods.forEach { period ->
                    if (period.second == array.size) {
                        if (cover[period.first] != state) {
                            applyPeriod(cover, period)
                            appliedPeriods.add(period)
                        }
                    } else {
                        applyPeriod(cover, period)
                        appliedPeriods.add(period)
                    }

                    if (array.contentEquals(cover)) {
                        return appliedPeriods
                    }
                }
            }

            CompositionMode.SIMPLE -> {
                periods.forEach { period ->
                    cover = applyPeriodOnlyIfChangesOccur(cover, period, appliedPeriods)

                    if (array.contentEquals(cover)) {
                        return appliedPeriods
                    }
                }
            }

            CompositionMode.GREEDY -> {
                do {
                    var maxDiff = 0
                    var bestPeriod = Pair(-1, -1)
                    periods.forEach { period ->
                        val newCover = cover.copyOf()
                        applyPeriod(newCover, period)
                        val diff = countDifferences(cover, newCover)
                        if (diff > maxDiff) {
                            bestPeriod = period
                            maxDiff = diff
                        }
                    }
                    applyPeriod(cover, bestPeriod)
                    appliedPeriods.add(bestPeriod)
                } while (!array.contentEquals(cover))
                return appliedPeriods
            }
        }

        if (array.contentEquals(cover)) {
            return appliedPeriods
        }

        var coverage = cover.count { it == state }.toDouble() / array.count { it == state }
        if (coverage.isNaN()) coverage = 0.0
        throw NoCoverFoundException("with coverage of $coverage")
    }

    private fun countDifferences(cover: BooleanArray, newCover: BooleanArray): Int {
        var count = 0
        for (i in cover.indices) {
            if (cover[i] != newCover[i]) count += 1
        }
        return count
    }

    private fun cleanMultiplesOfIntervals(periods: List<Pair<Int, Int>>, clean: Boolean): List<Pair<Int, Int>> {
        return if (clean) {
            val cleanPeriods = mutableListOf<Pair<Int, Int>>()
            periods.forEach { period ->
                if (!cleanPeriods.filter { it.first == period.first }.any { period.second / it.second % 2 == 0 }) {
                    cleanPeriods.add(period)
                }
            }
            cleanPeriods
        } else
            periods
    }

    private fun applyPeriodOnlyIfChangesOccur(cover: BooleanArray, period: Pair<Int, Int>, appliedPeriods: MutableSet<Pair<Int, Int>>): BooleanArray {
        val newCover = cover.copyInto(BooleanArray(cover.size))
        applyPeriod(newCover, period)
        return if (cover.contentEquals(newCover))
            cover
        else {
            appliedPeriods.add(period)
            newCover
        }
    }

    private fun applyPeriod(cover: BooleanArray, period: Pair<Int, Int>) {
        var position = period.first
        do {
            cover[position] = state
            position = (position + period.second) % cover.size
        } while (position != period.first)
    }

    private fun getPeriods(array: BooleanArray): List<Pair<Int, Int>> {
        val periods = mutableSetOf<Pair<Int, Int>>()
        for (factor in array.size.factorsSequence()) {
            for (index in 0 until factor) {
                if (array[index] == state && isPeriodic(array, index, factor)) {
                    periods.add(Pair(index % factor, factor))
                }
            }
        }
        return periods.toList()
    }

    private fun getPeriodsCO(array: BooleanArray): List<Pair<Int, Int>> {
        val jobs = mutableListOf<Deferred<List<Pair<Int, Int>>>>()
        val results = mutableListOf<Pair<Int, Int>>()
        for (factor in array.size.factorsSequence()) {
            jobs.add(computeAsync(array, factor))
        }
        runBlocking {
            jobs.forEach { results.addAll(it.await()) }
        }

        return results
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeAsync(array: BooleanArray, factor: Int): Deferred<List<Pair<Int, Int>>> = GlobalScope.async {
        val periods = mutableListOf<Pair<Int, Int>>()
        for (index in 0 until factor) {
            if (array[index] == state && isPeriodic(array, index, factor)) {
                periods.add(Pair(index % factor, factor))
            }
        }
        periods
    }

    private fun isPeriodic(array: BooleanArray, index: Int, factor: Int): Boolean {
        var pos = (index + factor) % array.size
        while (pos != index) {
            if (array[pos] != state) return false
            pos = (pos + factor) % array.size
        }
        return true
    }
}