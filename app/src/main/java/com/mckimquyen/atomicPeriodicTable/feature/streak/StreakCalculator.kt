package com.mckimquyen.atomicPeriodicTable.feature.streak

/**
 * Pure/Android-independent, same convention as VipCalculator/FlashcardScheduler/TrendsMapper.
 * epochDay is days-since-epoch (System.currentTimeMillis() / 86_400_000L), matching the
 * convention already used by ElementOfDay — not java.time.LocalDate (minSdk 24 < API 26).
 */
object StreakCalculator {
    fun updateStreak(lastEpochDay: Long, currentStreak: Int, todayEpochDay: Long): Int = when (todayEpochDay) {
        lastEpochDay -> currentStreak // already studied today, don't double-count
        lastEpochDay + 1 -> currentStreak + 1 // consecutive day
        else -> 1 // gap (or very first time ever, lastEpochDay defaults to 0)
    }
}
