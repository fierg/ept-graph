package io.github.fierg.model.result

data class Decomposition(
    val totalValues: Int,
    val periodSize: Int,
    val outliers: Collection<Int>,
    val cover: BooleanArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Decomposition

        if (totalValues != other.totalValues) return false
        if (periodSize != other.periodSize) return false
        if (outliers != other.outliers) return false
        return cover.contentEquals(other.cover)
    }

    override fun hashCode(): Int {
        var result = totalValues
        result = 31 * result + periodSize
        result = 31 * result + outliers.hashCode()
        result = 31 * result + cover.contentHashCode()
        return result
    }
}
