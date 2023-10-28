package io.github.fierg.algo

import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.*
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Operator
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.CleanQuotient
import io.github.fierg.model.result.Factor
import io.github.fierg.model.result.Cover
import kotlinx.coroutines.*

class Decomposer(state: Boolean = true, private val mode: CompositionMode = CompositionMode.SHORTEST_PERIODS, private val deltaWindowAlgo: Int = 0, private val threshold: Double = 1.0) {

    constructor(options: Options) : this(options.state, options.compositionMode, options.deltaWindowAlgo, options.threshold)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state
    private var singleDebugLog = true

    fun findComposite(graph: EPTGraph): Set<Cover> {
        val covers = mutableSetOf<Cover>()
        logInfo()

        graph.edges.forEach { edge ->
            try {
                covers.add(findCover(graph.steps[edge]!!))
                analyzeCover(covers.last())
            } catch (e: NoCoverFoundException) {
                Logger.error("${e.javaClass.simpleName} ${e.message} (edge length ${graph.steps[edge]!!.size})")
            }
        }

        return covers
    }

    fun findCover(input: BooleanArray): Cover {
        val periods = getFactorSequence(input.size).toList()
        val factorIndex = periods.mapIndexed { index, factor -> factor to index }.toMap()
        val factors = getFactors(input, factorIndex, periods)

        if (singleDebugLog) {
            Logger.debug("Using ${periods.size} periods: $periods")
            singleDebugLog = false
        }

        return getCover(input, factorIndex, factors, periods)
    }

    fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { input[it] == stateToReplace && expandedCover[it] != stateToReplace }
    }

    fun analyzeCover(result: Cover) {
        Logger.info(
            "Found decomposition with ${String.format("%3d", (result.periodSize.toDouble() / result.target.size * 100).toInt())}% original size (${String.format("%4d", result.periodSize)}), " +
                    "covered ${String.format("%4d", (result.totalValues - result.outliers.size))}/${String.format("%4d", result.totalValues)} values, " +
                    "resulting in ${String.format("%4d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
    }

    private fun getCover(input: BooleanArray, factorIndex: Map<Int, Int>, factors: Array<Factor>, periods: List<Int>): Cover {
        val cover = Cover(input, stateToReplace, if (this.mode == CompositionMode.CLEAN_QUOTIENTS) Operator.AND else Operator.OR)
        when (mode) {
            CompositionMode.MAX_DIVISORS -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!])
                    if (cover.outliers.size == 0) return cover
                }
                Logger.warn("No Exact Cover with max divisors only possible! Hard outliers (${cover.outliers.size})")
            }
            CompositionMode.SHORTEST_PERIODS -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!], skipFactorIfNoChangesOccur = true)
                    if (cover.getPrecision() >= threshold) return cover
                }
                Logger.warn("No Exact Cover with threshold $threshold possible! Hard outliers (${cover.outliers.size})")
            }
            CompositionMode.FOURIER_TRANSFORM -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!])
                    if (cover.outliers.size == 0) {
                        cover.fourierTransform(factorIndex)
                        return cover
                    }
                }
                Logger.warn("No Exact Cover possible! Hard outliers (${cover.outliers.size})")
            }
            CompositionMode.CLEAN_QUOTIENTS -> {
                factors.forEach { factor -> Logger.debug("${factor.cover.map { if (it) "1" else "0"  }}") }
                periods.forEach {size ->
                    if (size > 1) {
                        cover.addFactor(factors[factorIndex[size]!!])
                        if (cover.outliers.size == 0) {
                            return cover
                        }
                    }
                }
            }
        }
        return cover
    }

    private fun getFactors(input: BooleanArray, factorIndex: Map<Int, Int>, periods: List<Int>): Array<Factor> {
        val factors = Array(factorIndex.size) { Factor(BooleanArray(0), emptyList()) }
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in periods) {
            if (this.mode == CompositionMode.CLEAN_QUOTIENTS)
                jobs.add(computeCleanQuotients(input, factor, factors, factorIndex[factor]!!))
                else jobs.add(computeFactors(input, factor, factors, factorIndex[factor]!!))
        }
        runBlocking { jobs.forEach { it.await() } }
        return factors
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeCleanQuotients(input: BooleanArray, factorSize: Int, factors: Array<Factor>, factorIndex: Int) = GlobalScope.async {
        val coverArray = BooleanArray(factorSize) { !stateToReplace }
        for (index in input.indices) {
            val modIndex = index % factorSize
            if (input[index] == stateToReplace) coverArray[modIndex] = stateToReplace
        }
        factors[factorIndex] = Factor(coverArray, emptyList())
        return@async
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun computeFactors(input: BooleanArray, factorSize: Int, factors: Array<Factor>, factorIndex: Int) = GlobalScope.async {
        val coverArray = BooleanArray(factorSize) { !stateToReplace }
        for (index in 0 until factorSize) {
            if (input[index] == stateToReplace && isPeriodic(input, index, factorSize)) {
                coverArray[index % factorSize] = stateToReplace
            }
        }
        factors[factorIndex] = Factor(coverArray, getOutliers(input, coverArray))
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
            CompositionMode.SHORTEST_PERIODS, CompositionMode.FOURIER_TRANSFORM -> input.factors()
            CompositionMode.MAX_DIVISORS -> input.maximalDivisors()
            CompositionMode.CLEAN_QUOTIENTS -> input.upTo()
        }
    }

    private fun logInfo() {
        Logger.info("Searching for composite... (Looking for $stateToReplace values while decomposing)")
        when (mode) {
            CompositionMode.SHORTEST_PERIODS -> Logger.info("Trying to find cover with shortest possible factors with at least ${threshold * 100}% coverage.")
            CompositionMode.MAX_DIVISORS -> Logger.info("Trying to find cover with only the max divisors.")
            CompositionMode.FOURIER_TRANSFORM -> Logger.info("Trying to find most explainable factors using fourier transform.")
            CompositionMode.CLEAN_QUOTIENTS -> Logger.info("Trying to find clean quotients.")
        }
    }
}