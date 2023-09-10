package io.github.fierg.model.result

import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.minus
import io.github.fierg.extensions.removeIfNotIncludedIn

data class Cover(
    val target: BooleanArray,
    val stateToReplace: Boolean,
    val totalValues: Int,
    var periodSize: Int,
    val outliers: MutableList<Int>,
    val factors: MutableList<Factor>
) {

    private var lastOutlierSize = outliers.size

    constructor(input: BooleanArray, stateToReplace: Boolean) : this(
        input,
        stateToReplace,
        input.count { it == stateToReplace },
        0,
        input.mapIndexed { index, b -> if (b == stateToReplace) index else -1 }.filter { it != -1 }.toMutableList(),
        mutableListOf()
    )

    fun addFactor(factor: Factor, skipFactorIfNoChangesOccur: Boolean = false) {
        if (!skipFactorIfNoChangesOccur || factor.outliers.size < lastOutlierSize) {
            lastOutlierSize = factor.outliers.size
            factors.add(factor)
            periodSize = factor.cover.size
            outliers.removeIfNotIncludedIn(factor.outliers)
        }
    }

    fun getPrecision() = (totalValues - outliers.size).toDouble() / totalValues

    fun getCoverArray(): BooleanArray {
        val cover = BooleanArray(target.size) { !stateToReplace }
        factors.forEach { factor ->
            cover.applyPeriod(factor.cover, stateToReplace)
        }
        return cover
    }

    fun fourierTransform(factorIndex: Map<Int, Int>) {
        getMultiplesOfPeriods(factors.map { it.cover.size }).forEach { entry ->
            entry.value.forEach { multiple ->
                factors[factorIndex[multiple]!!].cover = cleanFactor(factors[factorIndex[entry.key]!!].cover, factors[factorIndex[multiple]!!].cover)
            }
        }
    }

    fun cleanFactor(pureFactor: BooleanArray, dirtyFactor: BooleanArray): BooleanArray {
        val extendedPureFactor = BooleanArray(dirtyFactor.size) {!stateToReplace}
        extendedPureFactor.applyPeriod(pureFactor, stateToReplace)
        return dirtyFactor - extendedPureFactor
    }


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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cover

        if (!target.contentEquals(other.target)) return false
        if (stateToReplace != other.stateToReplace) return false
        if (totalValues != other.totalValues) return false
        if (periodSize != other.periodSize) return false
        if (outliers != other.outliers) return false
        return factors == other.factors
    }

    override fun hashCode(): Int {
        var result = target.contentHashCode()
        result = 31 * result + stateToReplace.hashCode()
        result = 31 * result + totalValues
        result = 31 * result + periodSize
        result = 31 * result + outliers.hashCode()
        result = 31 * result + factors.hashCode()
        return result
    }
}
