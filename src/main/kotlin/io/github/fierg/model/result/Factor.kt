package io.github.fierg.model.result

import io.github.fierg.extensions.removeIfNotIncludedIn
import io.github.fierg.model.options.CompositionMode

/**
 * The `Factor` class represents a factor with a set of boolean values (cover) and a list of outliers.
 * It is a subclass of the `CleanQuotient` class, inheriting its properties and methods.
 *
 * @param array     The array of boolean values representing the factor.
 * @param outliers  The list of indices representing outliers in the factor.
 */
class Factor(var array: BooleanArray, val outliers: MutableList<Int>, val compositionMode: CompositionMode) {
    /**
     * Constructs a `Factor` object using a boolean array to represent the cover and a list of outliers.
     *
     * @param cover     The array of boolean values representing the factor.
     * @param outliers  The list of indices representing outliers in the factor.
     */
    constructor(cover: Array<Boolean>, outliers: MutableList<Int>, compositionMode: CompositionMode) : this(array = cover.toBooleanArray(), outliers, compositionMode)

    companion object {

        /**
         * Static method to calculate outliers for clean quotients using the provided parameters.
         *
         * @param target          The boolean array representing the target state.
         * @param stateToReplace  The boolean value to replace in the factors.
         * @param factors         A collection of boolean arrays representing factors.
         * @return A mutable list of integer indices representing outliers.
         */
        fun recalculateOutliers(target: BooleanArray, stateToReplace: Boolean, factors: Collection<BooleanArray>): MutableList<Int> {
            val outliers = mutableListOf<Int>()
            target.forEachIndexed { index, state ->
                if (state == stateToReplace) {
                    if (factors.all { factor -> factor[index % factor.size] != stateToReplace })
                        outliers.add(index)
                } else {
                    if (factors.all { factor -> factor[index % factor.size] == stateToReplace })
                        outliers.add(index)
                }
            }
            return outliers
        }
    }

    /**
     * Returns a string representation of the `Factor` object, displaying the cover as a list of 0s and 1s.
     *
     * @return A string representation of the `Factor` object.
     */
    override fun toString(): String {
        return "${array.map { if (it) "1" else "0" }}"
        //return "${cover.size}:${outliers.size}"
    }

    /**
     * Retrieves the boolean value at the specified index in the cover array.
     *
     * @param index The index of the element to retrieve.
     * @return The boolean value at the specified index in the cover array.
     */
    fun get(index: Int): Boolean {
        return array[index % array.size]
    }

    fun getRelativeSize(cover: Cover): Double {
        return array.size.toDouble() / cover.target.size
    }

    fun getRelativeCoveredValues(cover: Cover): Double {
        return cover.getRelativeCoveredValues(getOutliersOfCoverUntilThisFactor(cover))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Factor

        if (!array.contentEquals(other.array)) return false
        return outliers == other.outliers
    }

    fun getOutliersOfCoverUntilThisFactor(cover: Cover): MutableList<Int> {
        return when (this.compositionMode) {
            CompositionMode.OR -> {
                val outliers = cover.target.indices.filter { cover.target[it] == cover.stateToReplace }.toMutableList()
                val outliersOfFactors = cover.factors.subList(0, cover.factors.indexOf(this) + 1).map { it.outliers }
                outliersOfFactors.forEach { outliersList ->
                    outliers.removeIfNotIncludedIn(outliersList)
                }
                outliers
            }

            CompositionMode.AND -> {
                val factorArrays = cover.factors.subList(0, cover.factors.indexOf(this) + 1).map { it.array }
                recalculateOutliers(cover.target, cover.stateToReplace, factorArrays)
            }
        }

    }

    fun getCoveredValuesUntilThisFactor(cover: Cover) = cover.totalValues - getOutliersOfCoverUntilThisFactor(cover).size

}
