package io.github.fierg.periodic

import io.github.fierg.model.MSResult

class Periodic {

    private fun isPrefix(str: String, i: Int, k: Int): Boolean {
        var i = i
        if (i + k > str.length) return false
        for (j in 0 until k) {
            if (str[i] != str[j]) return false
            i++
        }
        return true
    }

    fun isKPeriodic(str: String, k: Int): Boolean {
        var i = k
        while (i < str.length) {
            if (!isPrefix(str, i, k)) return false
            i += k
        }
        return true
    }

    fun findShortestPeriod(str: String): Int {
        var k = 2
        while (k <= str.length) {
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
                        per = per + p
                        i = i - p
                        j = j - p
                    } else {
                        per = per + p
                        i = i - p
                        ms = 0
                        j = 1
                        k = 1
                        p = 1
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
            var i = 0
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