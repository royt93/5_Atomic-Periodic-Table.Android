package com.mckimquyen.atomicPeriodicTable.feature.reset

import android.content.Context
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.quiz.QuizBestScorePref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref

/**
 * Wipes gamification/achievement data — the exact 4 sources BadgeCalculator reads from
 * (streak, flashcard scheduling state, exam history, quiz best score), so unlocked badges
 * reset implicitly with no separate storage of their own to clear.
 *
 * Deliberately does NOT touch: NotesPref (user-authored content, not progress),
 * RecentlyViewedPref (browsing convenience, not an achievement), or VipPrefs (paid
 * entitlement — must survive a progress reset).
 */
object ProgressResetManager {
    fun resetAll(context: Context) {
        StudyStreakPref(context).restore(currentStreak = 0, lastEpochDay = 0L)
        FlashcardPref(context).clear()
        ExamHistoryPref(context).replaceHistory(emptyList())
        QuizBestScorePref(context).clear()
    }
}
