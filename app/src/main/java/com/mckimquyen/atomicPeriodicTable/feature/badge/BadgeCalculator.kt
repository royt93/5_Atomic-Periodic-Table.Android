package com.mckimquyen.atomicPeriodicTable.feature.badge

/**
 * Pure/Android-independent, same convention as StreakCalculator/MolarMassQuestionGenerator.
 * Badges are recomputed on-the-fly from existing progress data (streak/exam/flashcard prefs)
 * each time the badge screen opens — cheap to compute, so no separate "unlocked" state is
 * persisted for v1.
 */
object BadgeCalculator {
    fun computeUnlockedBadges(stats: BadgeStats): Set<BadgeId> {
        val unlocked = mutableSetOf<BadgeId>()
        if (stats.currentStreak >= 3) unlocked += BadgeId.STREAK_3
        if (stats.currentStreak >= 7) unlocked += BadgeId.STREAK_7
        if (stats.currentStreak >= 30) unlocked += BadgeId.STREAK_30
        if (stats.quizTotalQuestions > 0 && stats.bestQuizScore >= stats.quizTotalQuestions) unlocked += BadgeId.PERFECT_QUIZ
        if (stats.hasPerfectExam) unlocked += BadgeId.PERFECT_EXAM
        if (stats.flashcardsReviewedCount >= 50) unlocked += BadgeId.FLASHCARD_50
        if (stats.flashcardsReviewedCount >= 100) unlocked += BadgeId.FLASHCARD_100
        return unlocked
    }
}
