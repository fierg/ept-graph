package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.valueOfDeltaWindow
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.graph.SelfAwareEdge
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Decomposition
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class Decomposer(
    state: Boolean = true,
    private val deltaWindowAlgo: Int = 0,
    private val skipSingleStepEdges: Boolean = false,
    private val threshold: Double = 1.0
) {
    constructor(options: Options) : this(options.state, options.deltaWindowAlgo, options.skipSingleStepEdges, options.threshold)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state

    fun findComposite(graph: EPTGraph): Set<Decomposition> {
        Logger.info("Looking for $stateToReplace values while decomposing, threshold for cover: $threshold")

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
                    "covered ${String.format("%4d", (result.totalValues - result.outliers.size))}/${String.format("%4d", result.totalValues)} values, " +
                    "resulting in ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }


    fun findCover(input: BooleanArray): Decomposition {
        val periods = getPeriods(input)
        val valuesToCover = input.count { it == stateToReplace }

        input.size.factorsSequence().forEach { size ->
            val precision = (valuesToCover - periods[size]!!.outliers.size).toDouble() / valuesToCover
            if (precision >= threshold) return Decomposition(valuesToCover, size, periods[size]!!.outliers, periods[size]!!.cover)
        }

        return Decomposition(valuesToCover, input.size, periods[input.size]!!.outliers, periods[input.size]!!.cover)
    }

    private fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { expandedCover[it] != input[it] }
    }

    private fun getPeriods(input: BooleanArray): Map<Int, Cover> {
        val periodMap = ConcurrentHashMap<Int, Cover>()
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in input.size.factorsSequence()) {
            jobs.add(computeAsync(input, factor, periodMap))
        }

        runBlocking { jobs.forEach { it.await() } }

        if (periodMap.size < input.size.factorsSequence().toSet().size)
            Logger.error("Period map is missing some stuff...")

        return periodMap
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeAsync(input: BooleanArray, factor: Int, periodMap: MutableMap<Int, Cover>) = GlobalScope.async {
        val cover = BooleanArray(factor) { !stateToReplace }
        for (index in 0 until factor) {
            if (input[index] == stateToReplace && isPeriodic(input, index, factor)) {
                cover[index % factor] = stateToReplace
            }
        }
        periodMap[factor] = Cover(cover, getOutliers(input, cover))
        return@async
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