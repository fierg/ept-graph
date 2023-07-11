package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.contentEqualsWithDelta
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.valueOfDeltaWindow
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.graph.SelfAwareEdge
import kotlinx.coroutines.*

class Decomposer(
    state: Boolean = true,
    private val coroutines: Boolean = false,
    private val clean: Boolean = false,
    private val mode: CompositionMode = CompositionMode.ALL,
    private val deltaWindowAlgo: Int = 0,
    private val skipSingleStepEdges: Boolean = false
) {
    constructor(options: Options) : this(options.state, options.coroutines, options.clean, options.mode!!, options.deltaWindowAlgo, options.skipSingleStepEdges)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state

    fun findComposite(graph: EPTGraph): Set<Set<Triple<Int, Int, Int>>> {
        Logger.info("Looking for $stateToReplace values while decomposing.")
        if (coroutines) Logger.info("Using coroutines to compute periods.")
        if (clean) Logger.info("Cleaning up multiples/duplicates before applying the periods.")
        Logger.info("Choosing periods in $mode mode.")

        val decomposition = mutableSetOf<Set<Triple<Int, Int, Int>>>()

        graph.edges.forEach { edge ->
            try {
                if (!(skipSingleStepEdges && graph.steps[edge]!!.size <= 1)) {
                    val edgeDecomposition = findCover(graph.steps[edge]!!)
                    decomposition.add(edgeDecomposition)
                    analyze(graph, edge, edgeDecomposition)
                }
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return decomposition
    }

    fun analyze(graph: EPTGraph, edge: SelfAwareEdge, decomposition: Set<Triple<Int, Int, Int>>) {
        val valuesToCover = graph.steps[edge]!!.count { it == stateToReplace }
        val trivialPeriods = decomposition.count { it.second == graph.steps[edge]!!.size }

        Logger.info(
            "Found decomposition with ${String.format("%5d", decomposition.size)} periods, " +
                    "covered ${String.format("%5d", valuesToCover)} values, " +
                    "used ${String.format("%3d", ((trivialPeriods.toFloat() / decomposition.size) * 100).toInt())}% trivial periods."
        )
    }


    fun findCover(array: BooleanArray): Set<Triple<Int, Int, Int>> {
        val periods = cleanMultiplesOfIntervals(if (coroutines) getPeriodsCO(array) else getPeriods(array), clean)
        val cover = BooleanArray(array.size) { !stateToReplace }
        val appliedPeriods = mutableSetOf<Triple<Int, Int, Int>>()

        when (mode) {
            CompositionMode.ALL -> {
                periods.forEach { period ->
                    val changesMade = applyPeriod(cover, period)
                    appliedPeriods.add(Triple(period.first, period.second, changesMade))

                    if (applyDeltaWindow) {
                        if (array.contentEqualsWithDelta(cover, deltaWindowAlgo, stateToReplace)) return appliedPeriods
                    } else
                        if (array.contentEquals(cover)) return appliedPeriods
                }
            }

            CompositionMode.SIMPLE -> {
                periods.forEach { period ->
                    val changesMade = applyPeriod(cover, period)
                    if (changesMade > 0) appliedPeriods.add(Triple(period.first, period.second, changesMade))

                    if (applyDeltaWindow) {
                        if (array.contentEqualsWithDelta(cover, deltaWindowAlgo, stateToReplace)) return appliedPeriods
                    } else
                        if (array.contentEquals(cover)) return appliedPeriods
                }
            }

            CompositionMode.GREEDY -> {
                do {
                    var maxDiff = 0
                    var bestPeriod = Pair(-1, -1)
                    periods.forEach { period ->
                        val newCover = cover.copyOf()
                        val diff = applyPeriod(newCover, period)
                        if (diff > maxDiff) {
                            bestPeriod = period
                            maxDiff = diff
                        }
                    }
                    val changesMade = applyPeriod(cover, bestPeriod)
                    appliedPeriods.add(Triple(bestPeriod.first, bestPeriod.second, changesMade))
                } while (!array.contentEquals(cover))
                return appliedPeriods
            }

            CompositionMode.SET_COVER_ILP -> {
                val ilp = SetCoverILP(stateToReplace)
                ilp.getSetCoverInstanceFromPeriods(periods, array)
                val optimalPeriods = ilp.solveSetCover()
                optimalPeriods.forEach { set ->
                    val period = ilp.subSetMap!![set]!!
                    val changesMade = applyPeriod(cover, period)
                    appliedPeriods.add(Triple(period.first, period.second, changesMade))
                }
            }
        }

        if (array.contentEquals(cover)) {
            return appliedPeriods
        }
        var coverage = cover.count { it == stateToReplace }.toDouble() / array.count { it == stateToReplace }
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

    private fun applyPeriod(cover: BooleanArray, period: Pair<Int, Int>): Int {
        var position = period.first
        var changesMadeByPeriod = 0
        do {
            if (cover[position] != stateToReplace) {
                cover[position] = stateToReplace
                changesMadeByPeriod++
            }
            position = (position + period.second) % cover.size
        } while (position != period.first)
        return changesMadeByPeriod
    }

    private fun getPeriods(array: BooleanArray): List<Pair<Int, Int>> {
        val periods = mutableListOf<Pair<Int, Int>>()
        for (factor in array.size.factorsSequence()) {
            for (index in 0 until factor) {
                if (array[index] == stateToReplace && isPeriodic(array, index, factor)) {
                    periods.add(Pair(index % factor, factor))
                }
            }
        }
        return periods
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
            if (array[index] == stateToReplace && isPeriodic(array, index, factor)) {
                periods.add(Pair(index % factor, factor))
            }
        }
        periods
    }

    private fun isPeriodic(array: BooleanArray, index: Int, factor: Int): Boolean {
        var pos = (index + factor) % array.size
        while (pos != index) {
            if (applyDeltaWindow) {
                if (array.valueOfDeltaWindow(deltaWindowAlgo, index, stateToReplace)) return false
            } else
                if (array[pos] != stateToReplace) return false
            pos = (pos + factor) % array.size
        }
        return true
    }
}