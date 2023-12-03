package io.github.fierg.model.result

import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.minus
import io.github.fierg.extensions.removeIfNotIncludedIn
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.CompositionMode
import io.github.fierg.model.result.Factor.Companion.recalculateOutliers

/**
 * The `Cover` class represents a cover with various properties, including a target state, state to replace,
 * total values, size, outliers, a list of factors, and an operator.
 *
 * @param target            The boolean array representing the target state.
 * @param stateToReplace    The boolean value to replace in the cover.
 * @param totalValues       The total number of values in the cover.
 * @param size              The size of the cover.
 * @param outliers          A mutable list of integer indices representing outliers in the cover.
 * @param factors           A list of associated factors affecting the cover.
 * @param compositionMode          The logical operator used for combining factors (default is Operator.OR).
 */
data class Cover(
    val target: BooleanArray,
    val stateToReplace: Boolean,
    val totalValues: Int,
    var size: Int,
    var outliers: MutableList<Int>,
    val factors: MutableList<Factor>,
    val compositionMode: CompositionMode = CompositionMode.OR,
    val flippedState: Boolean = false,
    val deltaWindow: Int = 0
) {

    private var lastOutlierSize = outliers.size

    /**
     * Constructs a `Cover` object based on the input boolean array, state to replace, and operator.
     *
     * @param input             The boolean array representing the cover.
     * @param stateToReplace    The boolean value to replace in the cover.
     * @param compositionMode          The logical operator used for combining factors (default is Operator.OR).
     */
    constructor(input: BooleanArray, stateToReplace: Boolean, compositionMode: CompositionMode = CompositionMode.OR, deltaWindow: Int = 0) : this(
        input,
        stateToReplace,
        input.count { it == stateToReplace },
        0,
        input.mapIndexed { index, b -> if (b == stateToReplace) index else -1 }.filter { it != -1 }.toMutableList(),
        mutableListOf(),
        compositionMode,
        false,
        deltaWindow
    )

    /**
     * Adds a factor to the cover and updates its properties based on the operator and the factor's outliers.
     * Assumes factors are added in a sorted manner, from small to large.
     *
     * @param factor                    The factor to add to the cover.
     * @param skipFactorIfNoChangesOccur Whether to skip adding the factor if there are no changes in outliers.
     */
    fun addFactor(factor: Factor, skipFactorIfNoChangesOccur: Boolean = true) {
        when (this.compositionMode) {
            CompositionMode.OR -> {
                val newOutliers = outliers.toMutableList()
                newOutliers.removeIfNotIncludedIn(factor.outliers)
                if (!skipFactorIfNoChangesOccur || newOutliers.size < lastOutlierSize) {
                    factors.add(factor)
                    size = factor.array.size
                    lastOutlierSize = newOutliers.size
                    outliers = newOutliers
                }
            }

            CompositionMode.AND -> {
                if (!skipFactorIfNoChangesOccur || factor.array.any { value -> value == stateToReplace }) {
                    factors.add(factor)
                    size = factor.array.size
                    outliers = recalculateOutliers(target, stateToReplace, factors.map { it.array }, deltaWindow)
                } else {
                    Logger.debug("Skipping empty factor of size: ${factor.array.size}")
                }
            }
        }
    }

    /**
     * Calculates the precision of the cover based on the total values to cover and outliers.
     *
     * @return The precision of the cover as a double value.
     */
    fun getPrecision() = (totalValues - outliers.size).toDouble() / totalValues

    /**
     * Calculates the decomposition structure of the cover based on the total values to cover,
     * each factors size and outliers, according to the described metric.
     *
     * @return The decomposition structure of the cover as a double value.
     */
    fun getDecompositionStructure() = factors.fold(0.0) { acc, factor ->
        acc + (factor.array.size.toDouble() / target.size) * (factor.outliers.size.toDouble() / totalValues)
    }

    /**
     * Retrieves a boolean array representing the cover based on the operator and associated factors.
     *
     * @return A boolean array representing the cover.
     */
    fun getCoverArray(): BooleanArray {
        val cover = BooleanArray(target.size) { !stateToReplace }
        when (compositionMode) {
            CompositionMode.OR -> {
                cover.indices.forEach { index ->
                    cover[index] = factors.any { it.get(index) == stateToReplace }
                }
            }

            CompositionMode.AND -> {
                cover.indices.forEach { index ->
                    cover[index] = factors.all { it.get(index) != stateToReplace }
                }
            }
        }
        return cover
    }

    /**
     * Applies a Fourier transform to the factors to clean them of multiples.
     * Clean factors of multiples, e.g. if 10 and 101110 are both factors, 10 and 000100 are considered clean factors.
     */
    fun fourierTransform(factors: MutableList<Factor>, singleStateMode: Boolean = true): MutableList<Factor> {
        fourierTransformMultiples(factors)

        if (singleStateMode) {
            fullFourierTransform(factors)
        }
        return factors
    }

    private fun fullFourierTransform(factors: MutableList<Factor>) {
        val newCleanFactors = mutableListOf<Factor>()
        factors.forEach { factor ->
            val setValues = factor.array.indices.filter { index -> factor.array[index] == stateToReplace }.toMutableList()
            while (setValues.size > 1) {
                val position = setValues.removeLast()
                val newArray = BooleanArray(factor.array.size) { !stateToReplace }
                factor.array[position] = !stateToReplace
                newArray[position] = stateToReplace

                factor.outliers.addAll(getNewOutliers(position, factor.array.size, this.target.size))
                newCleanFactors.add(Factor(newArray, recalculateOutliers(this.target, stateToReplace, listOf(newArray), deltaWindow), compositionMode))
            }
        }
        factors.addAll(newCleanFactors)
        factors.sortBy { it.array.size }
    }

    private fun fourierTransformMultiples(factors: MutableList<Factor>) {
        val factorIndex = factors.mapIndexed { index, factor -> factor.array.size to index }.toMap()
        getMultiplesOfPeriods(factors.map { it.array.size }.filter { it != 1 }).forEach { entry ->
            entry.value.forEach { multiple ->
                factors[factorIndex[multiple]!!].array = cleanFactor(factors[factorIndex[entry.key]!!].array, factors[factorIndex[multiple]!!].array)
            }
        }
    }

    private fun getNewOutliers(position: Int, factorSize: Int, coverSize: Int): Collection<Int> {
        var pos = position
        val result = mutableListOf<Int>()
        while (pos < coverSize) {
            result.add(pos)
            pos += factorSize
        }
        return result
    }

    /**
     * Cleans a factor by removing all values from the dirty factor which are also covered by the pure factor.
     *
     * @param pureFactor    The pure factor to remove from the dirty factor.
     * @param dirtyFactor   The dirty factor to be cleaned.
     * @return A cleaned boolean array representing the factor.
     */
    fun cleanFactor(pureFactor: BooleanArray, dirtyFactor: BooleanArray): BooleanArray {
        val extendedPureFactor = BooleanArray(dirtyFactor.size) { !stateToReplace }
        extendedPureFactor.applyPeriod(pureFactor, stateToReplace)
        return dirtyFactor - extendedPureFactor
    }

    /**
     * Gets the multiples of periods from a list of periods.
     *
     * @param periods The list of periods to find multiples for.
     * @return A map containing periods and their multiples as lists.
     */
    private fun getMultiplesOfPeriods(periods: List<Int>): Map<Int, List<Int>> {
        val multiples = mutableMapOf<Int, MutableList<Int>>()
        periods.forEach { period ->
            val keys = multiples.keys.filter { period % it == 0 }
            if (keys.isEmpty()) {
                multiples[period] = mutableListOf()
            } else {
                keys.forEach { key ->
                    multiples[key]!!.add(period)
                    multiples[period] = mutableListOf()
                }
            }
        }
        return multiples
    }

    fun getAbsoluteResultMapFromCover(): Map<Double, Int> {
        val map = mutableMapOf<Double, Int>()
        this.factors.forEach { factor ->
            val size = factor.getRelativeSize(this)
            val coveredValues = factor.getCoveredValuesUntilThisFactor(this)
            map[size] = coveredValues
        }
        return map
    }

    /**
     * Checks if this `Cover` object is equal to another object.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cover

        if (!target.contentEquals(other.target)) return false
        if (stateToReplace != other.stateToReplace) return false
        if (totalValues != other.totalValues) return false
        if (size != other.size) return false
        if (outliers != other.outliers) return false
        return factors == other.factors
    }

    /**
     * Computes the hash code for this `Cover` object.
     *
     * @return The hash code for the object.
     */
    override fun hashCode(): Int {
        var result = target.contentHashCode()
        result = 31 * result + stateToReplace.hashCode()
        result = 31 * result + totalValues
        result = 31 * result + size
        result = 31 * result + outliers.hashCode()
        result = 31 * result + factors.hashCode()
        return result
    }

    fun getRelativeCoveredValues(outliers: MutableList<Int> = this.outliers): Double {
        assert(totalValues >= outliers.size)
        return ((totalValues - outliers.size).toDouble() / totalValues)
    }
}
