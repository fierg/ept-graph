package io.github.fierg.model.result

import io.github.fierg.extensions.applyPeriod
import io.github.fierg.extensions.removeIfNotIncludedIn

data class Cover(
    val target: BooleanArray,
    val stateToReplace: Boolean,
    val totalValues: Int,
    var periodSize: Int,
    val outliers: MutableList<Int>,
    val factors: MutableList<Factor>
) {
    constructor(input: BooleanArray, stateToReplace: Boolean) : this(
        input,
        stateToReplace,
        input.count { it == stateToReplace },
       0,
        input.mapIndexed { index, b -> if (b == stateToReplace) index else -1 }.filter { it != -1 }.toMutableList(),
        mutableListOf()
    )

    fun addFactor(factor: Factor) {
        factors.add(factor)
        periodSize = factor.cover.size
        outliers.removeIfNotIncludedIn(factor.outliers)
    }

    fun getPrecision() = (totalValues - outliers.size).toDouble() / totalValues

    fun getCoverArray(): BooleanArray {
        val cover = BooleanArray(16) {!stateToReplace}
        factors.forEach { factor ->
            cover.applyPeriod(factor.cover, stateToReplace)
        }
        return cover
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
