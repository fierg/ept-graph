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
        val covers = mutableSetOf<Cover>()
        logInfo()

        graph.edges.forEach { edge ->
            try {
                if (!(skipSingleStepEdges && graph.steps[edge]!!.size <= 1)) {
                    val input = graph.steps[edge]!!
                    val periods = getFactorSequence(input.size).toList()
                    val factorIndex = periods.mapIndexed { index, factor -> factor to index }.toMap()

                    Logger.debug("Using ${periods.size} periods: $periods")

                    val cover = findCover(graph.steps[edge]!!, factorIndex,  getFactors(input, factorIndex), periods)
                    analyzeCover(graph.steps[edge]!!.size, cover)
                    covers.add(cover)
                }
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return covers
    }

    fun findCover(input: BooleanArray): Cover {
        val periods = getFactorSequence(input.size).toList()
        val factorIndex = periods.mapIndexed { index, factor -> factor to index }.toMap()
        val factors = getFactors(input, factorIndex)
        return findCover(input, factorIndex, factors, periods)
    }

    fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { input[it] == stateToReplace && expandedCover[it] != stateToReplace }
    }

    fun analyzeCover(originalSize: Int, result: Cover) {
        Logger.info(
            "Found decomposition with ${String.format("%3d", (result.periodSize.toDouble() / originalSize * 100).toInt())}% original size, " +
                    "covered ${String.format("%4d", (result.totalValues - result.outliers.size))}/${String.format("%4d", result.totalValues)} values, " +
                    "resulting in ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }

    private fun findCover(input: BooleanArray, factorIndex: Map<Int, Int>, factors: Array<Factor>, periods: List<Int>): Cover {
        val cover = Cover(input, stateToReplace)

        when (mode) {
            CompositionMode.SHORTEST_PERIODS -> {
                var lastOutlierSize = Int.MAX_VALUE
                periods.forEach { size ->
                    if (factors[factorIndex[size]!!].outliers.size < lastOutlierSize) {
                        lastOutlierSize = factors[factorIndex[size]!!].outliers.size
                        cover.addFactor(factors[factorIndex[size]!!])
                        if (cover.getPrecision() >= threshold) return cover
                    }
                }
                throw NoCoverFoundException("No Exact Cover with threshold $threshold possible! Hard outliers (${cover.outliers.size}) $cover.outliers")
            }
            CompositionMode.MAX_DIVISORS -> {
                periods.forEach { size ->
                   cover.addFactor(factors[factorIndex[size]!!])
                    if (cover.outliers.size == 0) return cover
                }
                throw NoCoverFoundException("No Exact Cover only with max divisors possible! Hard outliers (${cover.outliers.size}) $cover.outliers")
            }
            CompositionMode.FOURIER_TRANSFORM -> {
                TODO()

            }
        }
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

    private fun logInfo() {
        Logger.info("Searching for composite... (Looking for $stateToReplace values while decomposing)")
        when (mode) {
            CompositionMode.SHORTEST_PERIODS -> Logger.info("Trying to find cover with shortest possible factors with at least ${threshold * 100}% coverage.")
            CompositionMode.MAX_DIVISORS -> Logger.info("Trying to find cover with only the max divisors.")
            CompositionMode.FOURIER_TRANSFORM -> Logger.info("Trying to find most explainable factors.")
        }
    }
}