package io.github.fierg.extensions

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

fun BooleanArray.applyPeriod(other: BooleanArray, state: Boolean) {
    assert(this.size % other.size == 0)
    val factor = this.size / other.size
    var offset = 0

    for (i in 0 until factor) {
        for (j in other.indices) {
            if (other[j] == state)
                this[j + offset] = state
        }
        offset += other.size
    }
}

/*
Lets you apply the & operator for BooleanArrays iff they are of same size.
 */
operator fun BooleanArray.plus(other: BooleanArray): BooleanArray {
    if (this.size != other.size) throw IllegalArgumentException("Arrays are not of same size.")
    val result = BooleanArray(this.size)
    for (i in indices) {
        result[i] = this[i] && other[i]
    }
    return result
}

operator fun BooleanArray.minus(other: BooleanArray): BooleanArray {
    if (this.size != other.size) throw IllegalArgumentException("Arrays are not of same size.")
    val result = BooleanArray(this.size)
    for (i in indices) {
        result[i] = this[i] && !other[i]
    }
    return result
}

fun BooleanArray.getBinaryString(): String {
    return this.map { if (it) "1" else "0" }.toString()
}