package io.github.fierg.extensions

/*
Finds all factors of a given number lazily and yields them into a sequence.
 */
fun Int.factorsSequence(includeSelf: Boolean = true): Sequence<Int> {
    val n = this
    return sequence {
        (1..n / 2).forEach {
            if (n % it == 0) yield(it)
        }
        if (includeSelf) yield(n)
    }
}

/*
Finds all prime factors of a given number lazily and yields them into a sequence.
 */
fun Int.primeFactors(): Sequence<Int> {
    var n: Int = this
    return sequence {
        for (i in 2..n / 2) {
            while (n % i == 0) {
                yield(i)
                n /= i
            }
        }
    }
}

/*
Finds all maximal divisors of a given number lazily and yields them into a sequence.
 */
fun Int.maximalDivisors(): Sequence<Int> {
    val primeFactors = this.primeFactors().toList().reversed()
    val primeFactorSet = primeFactors.toSet()

    return sequence {
        primeFactorSet.forEach { prime ->
            val indexToFilter = primeFactors.lastIndexOf(prime)
            yield(primeFactors.filterIndexed { index, _ -> index != indexToFilter }.fold(1) { acc: Int, i: Int -> acc * i })
        }
    }
}

fun Double.format(digits: Int = 5) = "%.${digits}f".format(this)