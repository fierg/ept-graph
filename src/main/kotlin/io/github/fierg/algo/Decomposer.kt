package io.github.fierg.algo

import io.github.fierg.data.F2FReader
import io.github.fierg.exceptions.NoCoverFoundException
import io.github.fierg.extensions.*
import io.github.fierg.graph.EPTGraph
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.DecompositionMode
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.options.Options
import io.github.fierg.model.result.Factor
import io.github.fierg.model.result.Cover
import kotlinx.coroutines.*

/**
 * The `Decomposer` class represents a decomposer that processes boolean arrays to find covers based on various composition modes and settings.
 *
 * @param state             The default state value (default is `true`).
 * @param mode              The composition mode used for decomposition (default is `CompositionMode.SHORTEST_PERIODS`).
 * @param deltaWindowAlgo   The delta window algorithm used (default is `0`).
 * @param threshold         The coverage threshold for finding covers (default is `1.0`).
 */
class Decomposer(state: Boolean = true, private val mode: DecompositionMode = DecompositionMode.GREEDY_SHORT_FACTORS, private val deltaWindowAlgo: Int = 0, private val threshold: Double = 1.0,
                 private val compositionMode: CompositionMode = CompositionMode.OR) {

    /**
     * Constructs a `Decomposer` object based on the provided `Options` object.
     *
     * @param options The `Options` object containing decomposition parameters.
     */
    constructor(options: Options) : this(options.state, options.decompositionMode, options.deltaWindowAlgo, options.threshold, options.compositionMode)

    private val applyDeltaWindow = deltaWindowAlgo > 0
    private val stateToReplace = !state
    private var singleDebugLog = true
    private var nrDigits = 3

    /**
     * Finds composite covers for a given graph and returns a list of `Cover` objects.
     *
     * @param graph The graph to find covers for.
     * @return A list of `Cover` objects representing composite covers.
     */
    fun findComposite(graph: EPTGraph): List<Cover> {
        val covers = mutableListOf<Cover>()

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

    /**
     * Finds a cover for the given boolean array based on the specified composition mode and settings.
     *
     * @param input The boolean array to find a cover for.
     * @return A `Cover` object representing the composite cover.
     */
    fun findCover(input: BooleanArray): Cover {
        nrDigits = input.size.digits()
        val periods = getFactorSequence(input.size).toList()
        val factorIndex = periods.mapIndexed { index, factor -> factor to index }.toMap()
        val factors = getFactors(input, factorIndex, periods)

        if (singleDebugLog) {
            logInfo()
            Logger.debug("Using ${periods.size} periods: $periods")
            singleDebugLog = false
        }

        return getCover(input, factorIndex, factors, periods)
    }

    /**
     * Retrieves a list of outlier indices in the input array based on the provided cover.
     *
     * @param input The input boolean array.
     * @param cover The cover to identify outliers.
     * @return A list of outlier indices.
     */
    fun getOutliers(input: BooleanArray, cover: BooleanArray): List<Int> {
        val expandedCover = BooleanArray(input.size) { !stateToReplace }
        expandedCover.applyPeriod(cover, stateToReplace)

        return expandedCover.indices.filter { input[it] == stateToReplace && expandedCover[it] != stateToReplace }
    }

    /**
     * Analyzes a `Cover` object and logs information about the decomposition.
     *
     * @param result The `Cover` object to analyze.
     */
    fun analyzeCover(result: Cover) {
        Logger.info(
            "Found decomposition with ${String.format("%${nrDigits}d", (result.size.toDouble() / result.target.size * 100).toInt())}% original size (${
                String.format(
                    "%${nrDigits}d",
                    result.size
                )
            }), " +
                    "covered ${String.format("%${nrDigits}d", (result.totalValues - result.outliers.size))}/${String.format("%${nrDigits}d", result.totalValues)} values, " +
                    "resulting in ${String.format("%${nrDigits}d", result.outliers.size)} outliers (${String.format("%3d", (result.outliers.size.toFloat() / result.totalValues * 100).toInt())}%)."
        )
        Logger.debug("Target:\t ${result.target.getBinaryString()}")
        Logger.debug("Cover:\t ${result.getCoverArray().getBinaryString()}")
        result.factors.forEach { factor ->
            Logger.debug("Factor:\t $factor -> Outliers: ${factor.outliers}")
        }
    }

    /**
     * Retrieves a composite cover for the input array based on the composition mode and settings.
     *
     * @param input         The input boolean array.
     * @param factorIndex   A map of factor sizes and their corresponding indices.
     * @param factors       An array of factors to consider.
     * @param periods       A list of period sizes to use for decomposition.
     * @return A `Cover` object representing the composite cover.
     */
    private fun getCover(input: BooleanArray, factorIndex: Map<Int, Int>, factors: Array<Factor>, periods: List<Int>): Cover {
        val cover = Cover(input, stateToReplace, compositionMode)
        when (mode) {
            DecompositionMode.MAX_DIVISORS -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!])
                    if (cover.outliers.size == 0) return cover
                }
                Logger.warn("No Exact Cover with max divisors only possible! Hard outliers (${cover.outliers.size})")
            }

            DecompositionMode.GREEDY_SHORT_FACTORS -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!], skipFactorIfNoChangesOccur = true)
                    if (cover.getPrecision() >= threshold) return cover
                }
                Logger.warn("No Exact Cover with threshold $threshold possible! Hard outliers (${cover.outliers.size})")
            }

            DecompositionMode.FOURIER_TRANSFORM -> {
                periods.forEach { size ->
                    cover.addFactor(factors[factorIndex[size]!!])
                    if (cover.outliers.size == 0) {
                        cover.fourierTransform()
                        return cover
                    }
                }
                Logger.warn("No Exact Cover possible! Hard outliers (${cover.outliers.size})")
            }
        }
        return cover
    }

    /**
     * Retrieves a list of factors for the input array based on the provided factor sizes and indices.
     *
     * @param input         The input boolean array.
     * @param factorIndex   A map of factor sizes and their corresponding indices.
     * @param periods       A list of period sizes to use for decomposition.
     * @return An array of `Factor` objects representing the factors.
     */
    private fun getFactors(input: BooleanArray, factorIndex: Map<Int, Int>, periods: List<Int>): Array<Factor> {
        val factors = Array(factorIndex.size) { Factor(BooleanArray(0), emptyList()) }
        val jobs = mutableListOf<Deferred<Unit>>()

        for (factor in periods) {
            when (this.compositionMode) {
                CompositionMode.AND -> jobs.add(computeFactorWithAndOperator(input, factor, factors, factorIndex[factor]!!))
                else -> jobs.add(computeFactorWithOrOperator(input, factor, factors, factorIndex[factor]!!))
            }
        }
        runBlocking { jobs.forEach { it.await() } }
        return factors
    }

    /**
     * Computes clean quotients asynchronously.
     *
     * @param input         The input boolean array.
     * @param factorIndex   A map of factor sizes and their corresponding indices.
     * @param factors       An array of factors to consider.
     * * @return An array of `Factor` objects representing the factors.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun computeFactorWithAndOperator(input: BooleanArray, factorSize: Int, factors: Array<Factor>, factorIndex: Int) = GlobalScope.async {
        val coverArray = BooleanArray(factorSize) { !stateToReplace }
        for (index in input.indices) {
            val modIndex = index % factorSize
            if (input[index] == stateToReplace) coverArray[modIndex] = stateToReplace
        }
        factors[factorIndex] = Factor(coverArray, Factor.getOutliersForCleanQuotients(input, stateToReplace, listOf(coverArray)))
        return@async
    }

    /**
     * Computes factors asynchronously.
     *
     * @param input         The input boolean array.
     * @param factorIndex   A map of factor sizes and their corresponding indices.
     * @param periods       A list of period sizes to use for decomposition.
     * @return An array of `Factor` objects representing the factors.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun computeFactorWithOrOperator(input: BooleanArray, factorSize: Int, factors: Array<Factor>, factorIndex: Int) = GlobalScope.async {
        val coverArray = BooleanArray(factorSize) { !stateToReplace }
        for (index in 0 until factorSize) {
            if (input[index] == stateToReplace && isPeriodic(input, index, factorSize)) {
                coverArray[index % factorSize] = stateToReplace
            }
        }
        factors[factorIndex] = Factor(coverArray, getOutliers(input, coverArray))
        return@async
    }

    /**
     * Checks if a given subsequence in the input array is periodic based on the specified factor size.
     *
     * @param array     The input boolean array.
     * @param index     The starting index of the subsequence to check.
     * @param factor    The factor size for the periodicity check.
     * @return `true` if the subsequence is periodic; otherwise, `false`.
     */
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

    /**
     * Retrieves a sequence of factor sizes based on the composition mode.
     *
     * @param input The size of the input array.
     * @return A sequence of factor sizes.
     */
    private fun getFactorSequence(input: Int): Sequence<Int> {
        return when (mode) {
            DecompositionMode.GREEDY_SHORT_FACTORS, DecompositionMode.FOURIER_TRANSFORM -> input.factors()
            DecompositionMode.MAX_DIVISORS -> input.maximalDivisors()
        }
    }

    /**
     * Logs information about the decomposition and the composition mode.
     */
    private fun logInfo() {
        Logger.info("Searching for composite... (Looking for $stateToReplace values while decomposing)")
        when (mode) {
            DecompositionMode.GREEDY_SHORT_FACTORS -> Logger.info("Trying to find cover with shortest possible factors with at least ${threshold * 100}% coverage.")
            DecompositionMode.MAX_DIVISORS -> Logger.info("Trying to find cover with only the max divisors.")
            DecompositionMode.FOURIER_TRANSFORM -> Logger.info("Trying to find most explainable factors using fourier transform.")
        }
    }

    /**
     * Analyzes multiple graphs and finds composite covers for each graph up to a specified index.
     *
     * @param upTo The index of the graph to analyze.
     * @return A list of lists of `Cover` objects representing composite covers for multiple graphs.
     */
    fun analyzeAllGraphs(upTo: Int): List<List<Cover>> {
        val result = mutableListOf<List<Cover>>()
        for (i in 0..upTo) {
            val f2fGraph = F2FReader().getF2FNetwork(i)
            result.add(findComposite(f2fGraph))
        }

        return result
    }
}