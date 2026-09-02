package com.mckimquyen.atomicPeriodicTable.feature.quiz

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Tracks the best score ever reached on the standard (non-practice) 10-question Quiz.
 * QuizAct itself never persisted a score before this — only Practice Exam attempts are
 * recorded (in ExamHistoryPref), so this exists purely to power the "Perfect Quiz" badge.
 */
class QuizBestScorePref(context: Context) {
    private val preference: SharedPreferences =
        context.getSharedPreferences("Quiz_Best_Score_Preference", Context.MODE_PRIVATE)

    fun getBestScore(): Int = preference.getInt("best_score", 0)

    fun recordScore(score: Int) {
        if (score > getBestScore()) {
            preference.edit { putInt("best_score", score) }
        }
    }

    fun clear() {
        preference.edit { clear() }
    }
}
