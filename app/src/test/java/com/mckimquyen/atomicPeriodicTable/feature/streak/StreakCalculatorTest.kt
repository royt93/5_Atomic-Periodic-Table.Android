package com.mckimquyen.atomicPeriodicTable.feature.streak

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun firstEverStudySession_startsStreakAtOne() {
        // lastEpochDay defaults to 0, a real todayEpochDay is always far from 0.
        assertEquals(1, StreakCalculator.updateStreak(lastEpochDay = 0L, currentStreak = 0, todayEpochDay = 20000L))
    }

    @Test
    fun consecutiveDay_incrementsStreak() {
        assertEquals(4, StreakCalculator.updateStreak(lastEpochDay = 20000L, currentStreak = 3, todayEpochDay = 20001L))
    }

    @Test
    fun sameDayAgain_doesNotDoubleCount() {
        assertEquals(3, StreakCalculator.updateStreak(lastEpochDay = 20000L, currentStreak = 3, todayEpochDay = 20000L))
    }

    @Test
    fun gapOfOneDay_resetsStreakToOne() {
        assertEquals(1, StreakCalculator.updateStreak(lastEpochDay = 20000L, currentStreak = 10, todayEpochDay = 20002L))
    }

    @Test
    fun largeGap_resetsStreakToOne() {
        assertEquals(1, StreakCalculator.updateStreak(lastEpochDay = 20000L, currentStreak = 50, todayEpochDay = 20100L))
    }

    @Test
    fun goingBackwardsInTime_treatedAsGap_resetsToOne() {
        // Defensive: a clock change or bad input shouldn't crash or grow the streak.
        assertEquals(1, StreakCalculator.updateStreak(lastEpochDay = 20000L, currentStreak = 5, todayEpochDay = 19999L))
    }

    @Test
    fun longConsecutiveRun_keepsIncrementing() {
        var streak = 0
        var lastDay = 0L
        for (day in 100L until 107L) {
            streak = StreakCalculator.updateStreak(lastDay, streak, day)
            lastDay = day
        }
        assertEquals(7, streak)
    }
}
