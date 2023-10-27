package io.github.fierg.model.result

class Factor(var cover: BooleanArray, val outliers: List<Int>) : CleanQuotient(cover) {
    constructor(cover: Array<Boolean>, outliers: List<Int>) : this(cover = cover.toBooleanArray(), outliers)

    override fun toString(): String {
        return "${cover.map { if (it) "1" else "0" }}:${outliers}"
        //return "${cover.size}:${outliers.size}"
    }

}
