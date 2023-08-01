package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.*
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Factor
import io.github.fierg.model.result.Cover
import kotlinx.coroutines.*

class Decomposer(state: Boolean = true, private val mode: CompositionMode = CompositionMode.SHORTEST_PERIODS, private val deltaWindowAlgo: Int = 0, private val skipSingleStepEdges: Boolean = false, private val threshold: Double = 1.0) {

    constructor(options: Options) : this(options.state, options.compositionMode, options.deltaWindowAlgo, options.skipSingleStepEdges, options.threshold)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state

    fun findComposite(graph: EPTGraph): Set<Cover> {
        Logger.info("Searching for composite... (Looking for $stateToReplace values while decomposing)")
        when (mode) {
            CompositionMode.SHORTEST_PERIODS -> Logger.debug("Trying to find shortest possible factors with at least ${threshold * 100}% coverage.")
            CompositionMode.MAX_DIVISORS -> Logger.debug("Trying to find cover with only the max divisors.")
            CompositionMode.FOURIER_TRANSFORM -> {}
        }
        val covers = mutableSetOf<Cover>()

        graph.edges.forEach { edge ->
            try {
                if (!(skipSingleStepEdges && graph.steps[edge]!!.size <= 1)) {
                    val cover = findCover(graph.steps[edge]!!)
                    covers.add(cover)
                    analyzeCover(graph.steps[edge]!!.size, cover)
                }
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return covers
    }

    fun findCover(input: BooleanArray): Cover {
        val factorIndex = getFactorSequence(input.size).mapIndexed { index, factor -> factor to index }.toMap()
        val factors = getFactors(input, factorIndex)
        val usedFactors = mutableListOf<Factor>()
        val valuesToCover = input.count { it == stateToReplace }
        val outliers = input.mapIndexed { index, b -> if (b == stateToReplace) index else -1 }.filter { it != -1 }.toMutableList()
        val periods = getFactorSequence(input.size).toList()
        Logger.debug("Using ${periods.size} periods: $periods")

        when (mode) {
            CompositionMode.SHORTEST_PERIODS -> {
                periods.forEach { size ->
                    usedFactors.add(factors[factorIndex[size]!!])
                    outliers.removeIfNotIncludedIn(factors[factorIndex[size]!!].outliers)
                    val precision = (valuesToCover - outliers.size).toDouble() / valuesToCover
                    if (precision >= threshold) return Cover(valuesToCover, size, factors[factorIndex[size]!!].outliers, factors[factorIndex[size]!!].cover, usedFactors)
                }
            }

            CompositionMode.MAX_DIVISORS -> {
                periods.forEach { size ->
                    usedFactors.add(factors[factorIndex[size]!!])
                    outliers.removeIfNotIncludedIn(factors[factorIndex[size]!!].outliers)
                    if (outliers.size == 0) return Cover(valuesToCover, size, factors[factorIndex[size]!!].outliers, factors[factorIndex[size]!!].cover, usedFactors)
                }
                throw NoCoverFoundException("No Exact Cover only with max divisors possible! Hard outliers (${outliers.size}) $outliers")
            }

            CompositionMode.FOURIER_TRANSFORM -> {
                TODO()

            }
        }
        throw NoCoverFoundException("Unknown Exception!")
    }

    fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { input[it] == stateToReplace && expandedCover[it] != stateToReplace }
    }

    private fun getFactors(input: BooleanArray, factorIndex: Map<Int, Int>): Array<Factor> {
        val factors = Array(factorIndex.size) { Factor(BooleanArray(0), emptyList()) }
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in getFactorSequence(input.size)) {
            jobs.add(computeFactors(input, factor, factors, factorIndex[factor]!!))
        }
        runBlocking { jobs.forEach { it.await() } }

        return factors
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeFactors(input: BooleanArray, factorSize: Int, factors: Array<Factor>, factorIndex: Int) = GlobalScope.async {
        val cover = BooleanArray(factorSize) { !stateToReplace }
        for (index in 0 until factorSize) {
            if (input[index] == stateToReplace && isPeriodic(input, index, factorSize)) {
                cover[index % factorSize] = stateToReplace
            }
        }
        factors[factorIndex] = Factor(cover, getOutliers(input, cover))
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

    private fun getFactorSequence(input: Int): Sequence<Int> {
        return when (mode) {
            CompositionMode.SHORTEST_PERIODS -> input.factorsSequence()
            CompositionMode.MAX_DIVISORS -> input.maximalDivisors()
            CompositionMode.FOURIER_TRANSFORM -> input.factorsSequence()
        }
    }

    fun analyzeCover(originalSize: Int, result: Cover) {
        Logger.info(
            "Found decomposition with ${String.format("%3d", (result.cover.size.toDouble() / originalSize * 100).toInt())}% original size, " +
                    "covered ${String.format("%4d", (result.totalValues - result.outliers.size))}/${String.format("%4d", result.totalValues)} values, " +
                    "resulting in ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }
}