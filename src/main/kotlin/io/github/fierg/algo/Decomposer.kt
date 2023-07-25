package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.valueOfDeltaWindow
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Options
import io.github.fierg.model.graph.SelfAwareEdge
import io.github.fierg.model.result.Decomposition
import kotlinx.coroutines.*

class Decomposer(
    state: Boolean = true,
    private val deltaWindowAlgo: Int = 0,
    private val skipSingleStepEdges: Boolean = false
) {
    constructor(options: Options) : this(options.state, options.deltaWindowAlgo, options.skipSingleStepEdges)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state

    fun findComposite(graph: EPTGraph): Set<Decomposition> {
        Logger.info("Looking for $stateToReplace values while decomposing.")

        val decompositions = mutableSetOf<Decomposition>()

        graph.edges.forEach { edge ->
            try {
                if (!(skipSingleStepEdges && graph.steps[edge]!!.size <= 1)) {
                    val edgeDecomposition = findCover(graph.steps[edge]!!)
                    decompositions.add(edgeDecomposition)
                    analyze(graph, edge, edgeDecomposition)
                }
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return decompositions
    }

    fun analyze(graph: EPTGraph, edge: SelfAwareEdge, result: Decomposition) {
        Logger.info(
            "Found decomposition with ${String.format("%3d", (result.cover.size.toDouble() / graph.steps[edge]!!.size * 100).toInt())}% original size, " +
                    "covered ${String.format("%5d", result.totalValues)} values, " +
                    "including ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }


    fun findCover(input: BooleanArray): Decomposition {
        val periods = getPeriods(input)
        val cover = BooleanArray(input.size) { !stateToReplace }
        val valuesToCover = input.count { it == stateToReplace }
        var lastAppliedSize = 0

        input.size.factorsSequence(false).forEach { size ->
            lastAppliedSize = size
            cover.applyPeriod(periods[size]!!, stateToReplace)
            if (input.contentEquals(cover)) return Decomposition(valuesToCover, lastAppliedSize, emptyList(), cover.copyOfRange(0, lastAppliedSize))
        }

        return Decomposition(valuesToCover, lastAppliedSize, getOutliers(input, cover), cover.copyOfRange(0, lastAppliedSize))
    }

    private fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)
        val outliers = mutableSetOf<Int>()

        return expandedCover.indices.filter { expandedCover[it] != input[it] }
    }

    private fun getPeriods(array: BooleanArray): MutableMap<Int, BooleanArray> {
        val periodMap = mutableMapOf<Int, BooleanArray>()
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in array.size.factorsSequence()) {
            periodMap[factor] = BooleanArray(factor) {!stateToReplace}
            jobs.add(computeAsync(array, factor, periodMap))
        }

        runBlocking { jobs.forEach { it.await() } }

        return periodMap
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeAsync(array: BooleanArray, factor: Int, periodMap: MutableMap<Int, BooleanArray>) = GlobalScope.async {
        for (index in 0 until factor) {
            if (array[index] == stateToReplace && isPeriodic(array, index, factor)) {
                periodMap[factor]!![index % factor] = stateToReplace
            }
        }
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