package io.github.fierg.extensions

/*
Finds all factors of a given number lazily and yields them into a sequence.
 */
fun Int.factorsSequence(includeLastValue: Boolean = true): Sequence<Int> {
    val n = this
    return sequence {
        (1..n / 2).forEach {
            if (n % it == 0) yield(it)
        }
        if (includeLastValue) yield(n)
    }
}

fun Double.format(digits: Int = 5) = "%.${digits}f".format(this)