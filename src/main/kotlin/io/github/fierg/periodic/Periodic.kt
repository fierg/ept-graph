package io.github.fierg.periodic

import io.github.fierg.model.MSResult

class Periodic {

    private fun isPrefix(str: BooleanArray, i: Int, k: Int): Boolean {
        var ii = i
        if (ii + k > str.size) return false
        for (j in 0 until k) {
            if (str[ii] != str[j]) return false
            ii++
        }
        return true
    }

    private fun isKPeriodic(str: BooleanArray, k: Int): Boolean {
        var i = k
        while (i < str.size) {
            if (!isPrefix(str, i, k)) return false
            i += k
        }
        return true
    }

    fun findShortestPeriod(str: BooleanArray): Int {
        var k = 1
        while (k <= str.size) {
            if (isKPeriodic(str, k)) return k
            else k++
        }
        return k
    }



    fun findPeriod(x: String): Int {
        var per = 1
        var i = 0
        var ms = 0
        var j = 1
        var k = 1
        var p = 1

        while (per + i + 1 <= x.length) {
            if (x[per + i + 1] == x[i + 1]) {
                i += 1
            } else {
                val result = nextMs(x.substring(1..i), ms, j, k, p)
                ms = result.ms
                j = result.j
                k = result.k
                p = result.p

                if ((x.substring(ms + 1..i) + x.substring(per + i + 1)).takeLast(k).endsWith(x.substring(1..ms))) {
                    if (j - ms > p) {
                        per += p
                        i -= p
                        j -= p
                    } else {
                        per += p
                        i -= p
                        ms = 0
                        j = 1
                    }
                }
                per += ms.coerceAtLeast((i - ms).coerceAtMost(j)) + 1
                i = 0
                ms = 0
                j = 1
                k = 1
                p = 1
            }
        }

        return per
    }

    private fun nextMs(x: String, ms: Int, j: Int, k: Int, p: Int): MSResult {
        var j1 = j
        var k1 = k
        var p1 = p
        while (j1 + k1 <= x.length) {
            val i = 0
            if (x[i + k1] == x[j1 + k1]) {
                if (k1 == p) {
                    j1 += k1
                    k1 = 1
                } else
                    k1 += 1
            } else if (x[i + k1] > x[j1 + k1]) {
                j1 += k1
                k1 = 1
                p1 = 1
            }
        }
        return MSResult(ms, j1, k1, p1)
    }
}