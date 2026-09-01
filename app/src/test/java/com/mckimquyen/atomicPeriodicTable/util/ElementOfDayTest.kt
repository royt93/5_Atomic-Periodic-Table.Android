package com.mckimquyen.atomicPeriodicTable.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementOfDayTest {

    @Test
    fun indexForDay_isDeterministic_forSameDay() {
        assertEquals(
            ElementOfDay.indexForDay(20000L, 118),
            ElementOfDay.indexForDay(20000L, 118),
        )
    }

    @Test
    fun indexForDay_changesWithDay() {
        val day1 = ElementOfDay.indexForDay(20000L, 118)
        val day2 = ElementOfDay.indexForDay(20001L, 118)
        assertTrue("consecutive days should not always collide", day1 != day2)
    }

    @Test
    fun indexForDay_staysWithinBounds_acrossManyDays() {
        val total = 118
        for (epochDay in 19700L..19700L + total * 3) {
            val index = ElementOfDay.indexForDay(epochDay, total)
            assertTrue("index $index out of bounds for epochDay $epochDay", index in 0 until total)
        }
    }

    @Test
    fun indexForDay_repeatsAfterFullCycle() {
        val total = 118
        val epochDay = 12345L
        assertEquals(
            ElementOfDay.indexForDay(epochDay, total),
            ElementOfDay.indexForDay(epochDay + total, total),
        )
    }
}
