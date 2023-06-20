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
        yield(n)
    }
}

fun Double.format(digits: Int = 5) = "%.${digits}f".format(this)

fun BooleanArray.contentEqualsWithDelta(other: BooleanArray, width: Int, state: Boolean): Boolean {
    if (this.size != other.size) {
        return false // Arrays must have the same length
    }
    // Iterate over the arrays
    for (i in indices) {
        val start = 0.coerceAtLeast(i - width)
        var foundMatch = false

        // Check for a match within the delta window
        for (j in start..i) {
            if (this[i] == state) {
                if (this[i] == other[j]) {
                    foundMatch = true
                    break
                }
            } else {
                foundMatch = true
                break
            }
        }
        // No match within the delta window
        if (!foundMatch) {
            return false
        }
    }
    // Arrays are equal within the delta window
    return true
}

fun BooleanArray.valueOfDeltaWindow(width: Int, index: Int, state: Boolean): Boolean {
    val end = (this.size - 1).coerceAtMost(index + width)

    for (i in index..end) {
        if (this[i] == state) return state
    }

    return !state
}
