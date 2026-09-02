package com.mckimquyen.atomicPeriodicTable.feature.badge

data class BadgeStats(
    val currentStreak: Int,
    val bestQuizScore: Int,
    val quizTotalQuestions: Int,
    val hasPerfectExam: Boolean,
    val flashcardsReviewedCount: Int,
)
