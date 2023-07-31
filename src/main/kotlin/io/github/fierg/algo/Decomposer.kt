package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.factorsSequence
import io.github.fierg.extensions.valueOfDeltaWindow
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Cover
import io.github.fierg.model.result.Decomposition
import kotlinx.coroutines.*

class Decomposer(state: Boolean = true, private val deltaWindowAlgo: Int = 0, private val skipSingleStepEdges: Boolean = false, private val threshold: Double = 1.0) {

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
                    analyze(graph.steps[edge]!!.size, edgeDecomposition)
                }
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return decompositions
    }

    fun analyze(originalSize: Int, result: Decomposition) {
        Logger.info(
            "Found decomposition with ${String.format("%3d", (result.cover.size.toDouble() / originalSize * 100).toInt())}% original size, " +
                    "covered ${String.format("%4d", (result.totalValues - result.outliers.size))}/${String.format("%4d", result.totalValues)} values, " +
                    "resulting in ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }

    fun findCover(input: BooleanArray): Decomposition {
        val periodIndex = input.size.factorsSequence().mapIndexed { index, factor -> factor to index }.toMap()
        val periods = getPeriods(input, periodIndex)
        val valuesToCover = input.count { it == stateToReplace }

        input.size.factorsSequence().forEach { size ->
            val precision = (valuesToCover - periods[periodIndex[size]!!].outliers.size).toDouble() / valuesToCover
            if (precision >= threshold) return Decomposition(valuesToCover, size, periods[periodIndex[size]!!].outliers, periods[periodIndex[size]!!].cover)
        }

        return Decomposition(valuesToCover, input.size, periods[periodIndex[input.size]!!].outliers, periods[periodIndex[input.size]!!].cover)
    }

    private fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { expandedCover[it] != input[it] }
    }

    private fun getPeriods(input: BooleanArray, periodIndex: Map<Int, Int>): Array<Cover> {
        val periods = Array(periodIndex.size) {Cover(BooleanArray(0), emptyList())}
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in input.size.factorsSequence()) {
            jobs.add(computeAsync(input, factor, periods, periodIndex))
        }

        runBlocking { jobs.forEach { it.await() } }
        if (periods.size < input.size.factorsSequence().toSet().size)
            Logger.error("Period map is missing some stuff...")

        return periods
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeAsync(input: BooleanArray, factor: Int, periods: Array<Cover>, periodIndex: Map<Int, Int>) = GlobalScope.async {
        val cover = BooleanArray(factor) { !stateToReplace }
        for (index in 0 until factor) {
            if (input[index] == stateToReplace && isPeriodic(input, index, factor)) {
                cover[index % factor] = stateToReplace
            }
        }
        periods[periodIndex[factor]!!] = Cover(cover, getOutliers(input, cover))
        return@async
    }

    private fun isPeriodic(array: BooleanArray, index: Int, factor: Int): Boolean {
        var position = (index + factor) % array.size
        while (position != index) {
            if (applyDeltaWindow) {
                if (array.valueOfDeltaWindow(deltaWindowAlgo, index, stateToReplace)) return false
            } else
                if (array[position] != stateToReplace) return false
            position = (position + factor) % array.size
        }
        return true
    }
}