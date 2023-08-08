package io.github.fierg.model.result

data class Factor(var cover: BooleanArray, val outliers: List<Int>) {

    constructor(cover: Array<Boolean>, outliers: List<Int>) : this(cover = cover.toBooleanArray(), outliers)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Factor

        if (!cover.contentEquals(other.cover)) return false
        return outliers == other.outliers
    }

    override fun hashCode(): Int {
        var result = cover.contentHashCode()
        result = 31 * result + outliers.hashCode()
        return result
    }

    override fun toString(): String {
        // return "${cover.map { if (it) "1" else "0" }}:${outliers}"
        return "${cover.size}:${outliers.size}"
    }

}
