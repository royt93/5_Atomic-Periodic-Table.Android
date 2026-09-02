package com.mckimquyen.atomicPeriodicTable.feature.badge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeCalculatorTest {

    private fun stats(
        currentStreak: Int = 0,
        bestQuizScore: Int = 0,
        quizTotalQuestions: Int = 10,
        hasPerfectExam: Boolean = false,
        flashcardsReviewedCount: Int = 0,
    ) = BadgeStats(currentStreak, bestQuizScore, quizTotalQuestions, hasPerfectExam, flashcardsReviewedCount)

    @Test
    fun noProgress_unlocksNoBadges() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats())
        assertTrue(unlocked.isEmpty())
    }

    @Test
    fun streakBelowThree_unlocksNoStreakBadge() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(currentStreak = 2))
        assertFalse(BadgeId.STREAK_3 in unlocked)
    }

    @Test
    fun streakOfThree_unlocksOnlyStreak3() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(currentStreak = 3))
        assertTrue(BadgeId.STREAK_3 in unlocked)
        assertFalse(BadgeId.STREAK_7 in unlocked)
        assertFalse(BadgeId.STREAK_30 in unlocked)
    }

    @Test
    fun streakOfSeven_unlocksStreak3And7ButNot30() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(currentStreak = 7))
        assertTrue(BadgeId.STREAK_3 in unlocked)
        assertTrue(BadgeId.STREAK_7 in unlocked)
        assertFalse(BadgeId.STREAK_30 in unlocked)
    }

    @Test
    fun streakOfThirty_unlocksAllThreeStreakBadges() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(currentStreak = 30))
        assertTrue(BadgeId.STREAK_3 in unlocked)
        assertTrue(BadgeId.STREAK_7 in unlocked)
        assertTrue(BadgeId.STREAK_30 in unlocked)
    }

    @Test
    fun bestQuizScoreBelowTotal_doesNotUnlockPerfectQuiz() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(bestQuizScore = 9, quizTotalQuestions = 10))
        assertFalse(BadgeId.PERFECT_QUIZ in unlocked)
    }

    @Test
    fun bestQuizScoreEqualsTotal_unlocksPerfectQuiz() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(bestQuizScore = 10, quizTotalQuestions = 10))
        assertTrue(BadgeId.PERFECT_QUIZ in unlocked)
    }

    @Test
    fun zeroTotalQuestions_neverUnlocksPerfectQuiz_evenWithZeroScore() {
        // Edge case: an uninitialized/degenerate quizTotalQuestions=0 must not trivially satisfy
        // "bestQuizScore >= quizTotalQuestions" (0 >= 0) and grant the badge for free.
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(bestQuizScore = 0, quizTotalQuestions = 0))
        assertFalse(BadgeId.PERFECT_QUIZ in unlocked)
    }

    @Test
    fun hasPerfectExamFalse_doesNotUnlockPerfectExam() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(hasPerfectExam = false))
        assertFalse(BadgeId.PERFECT_EXAM in unlocked)
    }

    @Test
    fun hasPerfectExamTrue_unlocksPerfectExam() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(hasPerfectExam = true))
        assertTrue(BadgeId.PERFECT_EXAM in unlocked)
    }

    @Test
    fun flashcardsReviewedBelowFifty_unlocksNoFlashcardBadge() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(flashcardsReviewedCount = 49))
        assertFalse(BadgeId.FLASHCARD_50 in unlocked)
    }

    @Test
    fun flashcardsReviewedFifty_unlocksFlashcard50ButNot100() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(flashcardsReviewedCount = 50))
        assertTrue(BadgeId.FLASHCARD_50 in unlocked)
        assertFalse(BadgeId.FLASHCARD_100 in unlocked)
    }

    @Test
    fun flashcardsReviewedHundred_unlocksBothFlashcardBadges() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats(flashcardsReviewedCount = 100))
        assertTrue(BadgeId.FLASHCARD_50 in unlocked)
        assertTrue(BadgeId.FLASHCARD_100 in unlocked)
    }

    @Test
    fun allThresholdsMet_unlocksAllSevenBadges() {
        val unlocked = BadgeCalculator.computeUnlockedBadges(
            stats(currentStreak = 30, bestQuizScore = 10, quizTotalQuestions = 10, hasPerfectExam = true, flashcardsReviewedCount = 100)
        )
        assertEquals(BadgeId.entries.toSet(), unlocked)
    }
}
