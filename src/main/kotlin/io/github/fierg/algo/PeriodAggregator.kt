package io.github.fierg.algo

import io.github.fierg.extensions.applyPeriod

class PeriodAggregator(private val sizes: Sequence<Int>, private val stateToReplaceWith: Boolean) {

    private val periodMap = mutableMapOf<Int,BooleanArray>()

    init {
        sizes.forEach { size ->
            periodMap[size] = BooleanArray(size) {!stateToReplaceWith}
        }
    }

    fun addPeriod(period: Pair<Int,Int>) {
        periodMap[period.second]!![period.first] = stateToReplaceWith
    }

    fun findShortestCover(cover: BooleanArray): Int {
        val array = BooleanArray(cover.size) {!stateToReplaceWith}

        sizes.forEach { size ->
            array.applyPeriod(periodMap[size]!!, stateToReplaceWith)
            if (array.contentEquals(cover)) return size
        }
        return -1
    }
}