package io.github.fierg.model.result

import io.github.fierg.logger.Logger

class Factor(var cover: BooleanArray, val outliers: List<Int>) : CleanQuotient(cover) {
    constructor(cover: Array<Boolean>, outliers: List<Int>) : this(cover = cover.toBooleanArray(), outliers)

    companion object {
        fun getOutliersForCleanQuotients(target: BooleanArray, stateToReplace: Boolean, factors: Collection<BooleanArray>): MutableList<Int> {
            val outliers = mutableListOf<Int>()
            target.forEachIndexed { index, state ->
                if (state == stateToReplace) {
                    if (factors.any { factor -> factor[index % factor.size] != stateToReplace })
                        outliers.add(index)
                } else {
                    if (factors.all { factor -> factor[index % factor.size] == stateToReplace })
                        outliers.add(index)
                }
            }
            return outliers
        }
    }

    override fun toString(): String {
        return "${cover.map { if (it) "1" else "0" }}"
        //return "${cover.size}:${outliers.size}"
    }

    fun get(index: Int): Boolean {
        return cover[index % cover.size]
    }
}
