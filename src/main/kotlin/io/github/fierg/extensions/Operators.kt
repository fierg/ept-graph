package io.github.fierg.extensions

/*
Lets you apply the & operator for BooleanArrays iff they are of same size.
 */
operator fun BooleanArray.plus(other: BooleanArray): BooleanArray {
    if (this.size != other.size) throw IllegalArgumentException("Arrays are not of same size.")
    val result = BooleanArray(this.size)
    for (i in 0 until  this.size) {
        result[i] = this[i] && other[i]
    }
    return result
}

operator fun BooleanArray.minus(other: BooleanArray): BooleanArray {
    if (this.size != other.size) throw IllegalArgumentException("Arrays are not of same size.")
    val result = BooleanArray(this.size)
    for (i in 0 until  this.size) {
        result[i] = this[i] && !other[i]
    }
    return result
}