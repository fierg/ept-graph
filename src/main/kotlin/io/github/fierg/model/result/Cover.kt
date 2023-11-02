package io.github.fierg.model.result

import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.minus
import io.github.fierg.extensions.removeIfNotIncludedIn
import io.github.fierg.logger.Logger
import io.github.fierg.model.options.Operator

data class Cover(
    val target: BooleanArray,
    val stateToReplace: Boolean,
    val totalValues: Int,
    var size: Int,
    var outliers: MutableList<Int>,
    val factors: MutableList<Factor>,
    val operator: Operator = Operator.OR
) {

    private var lastOutlierSize = outliers.size

    constructor(input: BooleanArray, stateToReplace: Boolean, operator: Operator = Operator.OR) : this(
        input,
        stateToReplace,
        input.count { it == stateToReplace },
        0,
        input.mapIndexed { index, b -> if (b == stateToReplace) index else -1 }.filter { it != -1 }.toMutableList(),
        mutableListOf(),
        operator
    )

    fun addFactor(factor: Factor, skipFactorIfNoChangesOccur: Boolean = true) {
        when (this.operator) {
            Operator.OR -> {
                if (!skipFactorIfNoChangesOccur || factor.outliers.size < lastOutlierSize) {
                    factors.add(factor)
                    size = factor.cover.size
                    lastOutlierSize = factor.outliers.size
                    outliers.removeIfNotIncludedIn(factor.outliers)
                }
            }

            Operator.AND -> {
                if (!skipFactorIfNoChangesOccur || !outliers.any { factor.outliers.contains(it) }) {
                    factors.add(factor)
                    size = factor.cover.size
                    outliers = Factor.getOutliersForCleanQuotients(target, stateToReplace, factors.map { it.cover })
                }
            }
        }
    }

    fun getPrecision() = (totalValues - outliers.size).toDouble() / totalValues

    fun getCoverArray(): BooleanArray {
        val cover = BooleanArray(target.size) { !stateToReplace }
        when (operator) {
            Operator.OR -> {
                factors.forEach { factor ->
                    cover.applyPeriod(factor.cover, stateToReplace)
                }
            }
            Operator.AND ->{
                cover.indices.forEach { index ->
                    if (factors.all { it.cover[index % it.cover.size] }) cover[index] = true
                }
            }
        }
        return cover
    }

    fun fourierTransform() {
        //Clean factors of multiples, e.g. if 10 and 101110 are both factors, 10 and 000100 are considered clean factors.
        val factorIndex = factors.mapIndexed { index, factor -> factor.cover.size to index }.toMap()
        getMultiplesOfPeriods(factors.map { it.cover.size }).forEach { entry ->
            entry.value.forEach { multiple ->
                factors[factorIndex[multiple]!!].cover = cleanFactor(factors[factorIndex[entry.key]!!].cover, factors[factorIndex[multiple]!!].cover)
            }
        }
    }

    fun cleanFactor(pureFactor: BooleanArray, dirtyFactor: BooleanArray): BooleanArray {
        val extendedPureFactor = BooleanArray(dirtyFactor.size) { !stateToReplace }
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
        if (size != other.size) return false
        if (outliers != other.outliers) return false
        return factors == other.factors
    }

    override fun hashCode(): Int {
        var result = target.contentHashCode()
        result = 31 * result + stateToReplace.hashCode()
        result = 31 * result + totalValues
        result = 31 * result + size
        result = 31 * result + outliers.hashCode()
        result = 31 * result + factors.hashCode()
        return result
    }
}
