package com.mckimquyen.atomicPeriodicTable.feature.streak

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Shared streak across Flashcard + Quiz (one streak for "studied today", not per-feature) —
 * flat-key SharedPreferences, same convention as NotesPref/FlashcardPref.
 */
class StudyStreakPref(context: Context) {
    private val preference: SharedPreferences =
        context.getSharedPreferences("Study_Streak_Preference", Context.MODE_PRIVATE)

    fun getCurrentStreak(): Int = preference.getInt("current_streak", 0)

    fun getLastEpochDay(): Long = preference.getLong("last_study_epoch_day", 0L)

    /** Writes raw values directly (no streak-continuation logic) — for restoring from a backup. */
    fun restore(currentStreak: Int, lastEpochDay: Long) {
        preference.edit {
            putInt("current_streak", currentStreak)
            putLong("last_study_epoch_day", lastEpochDay)
        }
    }

    /** Call once whenever the user completes a study action (a flashcard rating, a finished quiz). */
    fun recordStudyToday(todayEpochDay: Long = System.currentTimeMillis() / 86_400_000L): Int {
        val lastEpochDay = preference.getLong("last_study_epoch_day", 0L)
        val currentStreak = preference.getInt("current_streak", 0)
        val newStreak = StreakCalculator.updateStreak(lastEpochDay, currentStreak, todayEpochDay)
        preference.edit {
            putLong("last_study_epoch_day", todayEpochDay)
            putInt("current_streak", newStreak)
        }
        return newStreak
    }
}
