package com.mckimquyen.atomicPeriodicTable.feature.compare

enum class Rank { HIGH, LOW, NEUTRAL }

/**
 * Pure/Android-independent ranking for the 3-way compare table (mục 16): with 3+ values a single
 * shared ▲/▼ arrow can no longer identify which column it refers to, so each value is ranked
 * against the others instead (max → HIGH, min → LOW, everything else → NEUTRAL).
 */
object CompareRanking {
    fun rank(values: List<Double?>): List<Rank> {
        val numeric = values.filterNotNull()
        if (numeric.size < 2) return values.map { Rank.NEUTRAL }

        val max = numeric.max()
        val min = numeric.min()
        if (max == min) return values.map { Rank.NEUTRAL }

        return values.map { v ->
            when {
                v == null -> Rank.NEUTRAL
                v == max -> Rank.HIGH
                v == min -> Rank.LOW
                else -> Rank.NEUTRAL
            }
        }
    }
}
