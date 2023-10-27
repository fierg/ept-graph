package io.github.fierg.model.result

open class CleanQuotient(val quotient: BooleanArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CleanQuotient

        return quotient.contentEquals(other.quotient)
    }

    override fun hashCode(): Int {
        return quotient.contentHashCode()
    }

    fun toFactor() = Factor(quotient, emptyList())

}