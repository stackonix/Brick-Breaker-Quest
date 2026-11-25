package com.example.domain.engine

/**
 * Deterministic SplitMix64 PRNG so identical seeds (Daily Puzzle epoch day or Friend Challenge code)
 * generate the exact same puzzles across all Android devices completely offline.
 */
class DeterministicRandom(seed: Long) {
    private var state: Long = seed xor -7046029254386353131L

    fun nextLong(): Long {
        state += -7046029254386353131L
        var z = state
        z = (z xor (z ushr 30)) * -4658895280553007687L
        z = (z xor (z ushr 27)) * -7723592293110705685L
        return z xor (z ushr 31)
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        val r = nextLong() ushr 1
        return (r % bound).toInt()
    }

    fun nextInt(from: Int, until: Int): Int {
        require(until > from) { "until must be > from" }
        return from + nextInt(until - from)
    }

    fun nextFloat(): Float {
        return ((nextLong() ushr 40).toInt()) / 16777216.0f
    }

    fun <T> shuffle(list: List<T>): List<T> {
        val mutable = list.toMutableList()
        for (i in mutable.lastIndex downTo 1) {
            val j = nextInt(i + 1)
            val tmp = mutable[i]
            mutable[i] = mutable[j]
            mutable[j] = tmp
        }
        return mutable
    }

    fun <T> pick(list: List<T>): T = list[nextInt(list.size)]
}
