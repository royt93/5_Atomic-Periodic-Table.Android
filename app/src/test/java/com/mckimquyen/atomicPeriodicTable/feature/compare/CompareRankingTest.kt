package com.mckimquyen.atomicPeriodicTable.feature.compare

import org.junit.Assert.assertEquals
import org.junit.Test

class CompareRankingTest {

    @Test
    fun threeDistinctValues_highestHigh_lowestLow_middleNeutral() {
        val result = CompareRanking.rank(listOf(5.0, 1.0, 3.0))
        assertEquals(listOf(Rank.HIGH, Rank.LOW, Rank.NEUTRAL), result)
    }

    @Test
    fun twoValues_higherHigh_lowerLow_matchesOriginalPairwiseSemantics() {
        val result = CompareRanking.rank(listOf(10.0, 2.0))
        assertEquals(listOf(Rank.HIGH, Rank.LOW), result)
    }

    @Test
    fun allValuesEqual_allNeutral() {
        val result = CompareRanking.rank(listOf(7.0, 7.0, 7.0))
        assertEquals(listOf(Rank.NEUTRAL, Rank.NEUTRAL, Rank.NEUTRAL), result)
    }

    @Test
    fun twoOfThreeTiedForHighest_bothGetHigh() {
        // Regression guard for a tie at the max: both 9.0s must be HIGH, not just the first one.
        val result = CompareRanking.rank(listOf(9.0, 9.0, 1.0))
        assertEquals(listOf(Rank.HIGH, Rank.HIGH, Rank.LOW), result)
    }

    @Test
    fun missingValueExcludedFromRanking_stillNeutral() {
        val result = CompareRanking.rank(listOf(5.0, null, 1.0))
        assertEquals(listOf(Rank.HIGH, Rank.NEUTRAL, Rank.LOW), result)
    }

    @Test
    fun fewerThanTwoNumericValues_allNeutral() {
        assertEquals(listOf(Rank.NEUTRAL, Rank.NEUTRAL, Rank.NEUTRAL), CompareRanking.rank(listOf(5.0, null, null)))
        assertEquals(listOf(Rank.NEUTRAL, Rank.NEUTRAL), CompareRanking.rank(listOf(null, null)))
    }

    @Test
    fun negativeValues_rankedCorrectly() {
        val result = CompareRanking.rank(listOf(-5.0, -1.0, -10.0))
        assertEquals(listOf(Rank.NEUTRAL, Rank.HIGH, Rank.LOW), result)
    }
}
