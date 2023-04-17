package io.github.fierg.extensions

/*
Finds all factors of a given number lazily and yields them into a sequence.
 */
fun Int.factorsSequence(): Sequence<Int> {
    val n = this
    return sequence {
        (1..n / 2).forEach {
            if (n % it == 0) yield(it)
        }
    }
}



/*
Lets you apply the && operator for BooleanArrays iff they are of same size.
 */
operator fun BooleanArray.plus(other: BooleanArray): BooleanArray {
    if (this.size != other.size) throw IllegalArgumentException("Arrays are not of same size.")
    val result = BooleanArray(this.size)
    for (i in 0..this.size) {
        result[i] = this[i] && other[i]
    }
    return result
}