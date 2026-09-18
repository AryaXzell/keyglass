package com.aryaxzell.keyglass.data.engine

/**
 * Zero-allocation high-performance Levenshtein distance calculator.
 * Uses two 1D primitive arrays with buffer swapping to eliminate GC overhead.
 */
object Levenshtein {
    fun distance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        if (len1 == 0) return len2
        if (len2 == 0) return len1

        var p = IntArray(len2 + 1)
        var d = IntArray(len2 + 1)

        for (i in 0..len2) {
            p[i] = i
        }

        for (i in 1..len1) {
            val c1 = s1[i - 1]
            d[0] = i
            for (j in 1..len2) {
                val cost = if (c1 == s2[j - 1]) 0 else 1
                d[j] = minOf(
                    d[j - 1] + 1,
                    p[j] + 1,
                    p[j - 1] + cost
                )
            }
            val swap = p
            p = d
            d = swap
        }

        return p[len2]
    }
}
