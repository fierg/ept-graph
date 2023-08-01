package io.github.fierg.model.result

data class Cover(
    val totalValues: Int,
    val periodSize: Int,
    val outliers: Collection<Int>,
    val cover: BooleanArray,
    val factors: Collection<Factor>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cover

        if (totalValues != other.totalValues) return false
        if (periodSize != other.periodSize) return false
        if (outliers != other.outliers) return false
        if (!cover.contentEquals(other.cover)) return false
        return factors == other.factors
    }

    override fun hashCode(): Int {
        var result = totalValues
        result = 31 * result + periodSize
        result = 31 * result + outliers.hashCode()
        result = 31 * result + cover.contentHashCode()
        result = 31 * result + factors.hashCode()
        return result
    }
}
