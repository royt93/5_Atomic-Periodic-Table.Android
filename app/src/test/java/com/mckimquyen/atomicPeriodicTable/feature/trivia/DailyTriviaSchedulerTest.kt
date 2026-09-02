package com.mckimquyen.atomicPeriodicTable.feature.trivia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DailyTriviaSchedulerTest {

    private fun calendarAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }

    @Test
    fun beforeTriggerTimeToday_schedulesLaterToday() {
        val now = calendarAt(2026, Calendar.MARCH, 15, 7, 0) // 7:00 AM, before 9:00 trigger
        val result = Calendar.getInstance().apply { timeInMillis = DailyTriviaScheduler.nextTriggerAtMillis(now.timeInMillis) }

        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, result.get(Calendar.MONTH))
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(DailyTriviaScheduler.TRIGGER_HOUR, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(DailyTriviaScheduler.TRIGGER_MINUTE, result.get(Calendar.MINUTE))
    }

    @Test
    fun afterTriggerTimeToday_schedulesTomorrow() {
        val now = calendarAt(2026, Calendar.MARCH, 15, 14, 30) // 2:30 PM, after 9:00 trigger
        val result = Calendar.getInstance().apply { timeInMillis = DailyTriviaScheduler.nextTriggerAtMillis(now.timeInMillis) }

        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(DailyTriviaScheduler.TRIGGER_HOUR, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(DailyTriviaScheduler.TRIGGER_MINUTE, result.get(Calendar.MINUTE))
    }

    @Test
    fun exactlyAtTriggerTime_treatedAsAlreadyPassed_schedulesTomorrow() {
        val now = calendarAt(2026, Calendar.MARCH, 15, DailyTriviaScheduler.TRIGGER_HOUR, DailyTriviaScheduler.TRIGGER_MINUTE)
        val result = Calendar.getInstance().apply { timeInMillis = DailyTriviaScheduler.nextTriggerAtMillis(now.timeInMillis) }

        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun monthBoundary_rollsOverCorrectly() {
        val now = calendarAt(2026, Calendar.MARCH, 31, 23, 0) // last day of March, after trigger
        val result = Calendar.getInstance().apply { timeInMillis = DailyTriviaScheduler.nextTriggerAtMillis(now.timeInMillis) }

        assertEquals(Calendar.APRIL, result.get(Calendar.MONTH))
        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun resultIsAlwaysStrictlyAfterNow() {
        val seeds = listOf(0, 6, 9, 9 * 60 + 1, 23 * 60).map { minutesSinceMidnight ->
            calendarAt(2026, Calendar.JUNE, 10, minutesSinceMidnight / 60, minutesSinceMidnight % 60).timeInMillis
        }
        for (now in seeds) {
            assertTrue(DailyTriviaScheduler.nextTriggerAtMillis(now) > now)
        }
    }
}
