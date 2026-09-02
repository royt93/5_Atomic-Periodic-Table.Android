package com.mckimquyen.atomicPeriodicTable.feature.history

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentlyViewedPrefTest {

    @Test
    fun newSymbol_addedToFront() {
        val result = RecentlyViewedPref.withRecorded(listOf("He", "Li"), "H")
        assertEquals(listOf("H", "He", "Li"), result)
    }

    @Test
    fun emptyList_singleSymbolAdded() {
        val result = RecentlyViewedPref.withRecorded(emptyList(), "H")
        assertEquals(listOf("H"), result)
    }

    @Test
    fun existingSymbol_movedToFront_notDuplicated() {
        val result = RecentlyViewedPref.withRecorded(listOf("He", "H", "Li"), "H")
        assertEquals(listOf("H", "He", "Li"), result)
    }

    @Test
    fun viewingSameSymbolTwiceInARow_isANoOp() {
        val once = RecentlyViewedPref.withRecorded(listOf("He"), "H")
        val twice = RecentlyViewedPref.withRecorded(once, "H")
        assertEquals(once, twice)
    }

    @Test
    fun exceedingMaxRecent_dropsOldestFromTheEnd() {
        val full = (1..RecentlyViewedPref.MAX_RECENT).map { "E$it" }
        val result = RecentlyViewedPref.withRecorded(full, "NEW")
        assertEquals(RecentlyViewedPref.MAX_RECENT, result.size)
        assertEquals("NEW", result.first())
        assertEquals("E${RecentlyViewedPref.MAX_RECENT - 1}", result.last()) // oldest (E10) dropped
    }

    @Test
    fun customMaxRecent_respected() {
        val result = RecentlyViewedPref.withRecorded(listOf("A", "B", "C"), "D", maxRecent = 2)
        assertEquals(listOf("D", "A"), result)
    }
}
