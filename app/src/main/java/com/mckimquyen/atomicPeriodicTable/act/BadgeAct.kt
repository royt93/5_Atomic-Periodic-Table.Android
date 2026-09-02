package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ABadgesBinding
import com.mckimquyen.atomicPeriodicTable.feature.badge.BadgeCalculator
import com.mckimquyen.atomicPeriodicTable.feature.badge.BadgeId
import com.mckimquyen.atomicPeriodicTable.feature.badge.BadgeStats
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.quiz.QuizBestScorePref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel

/** Grid of fixed, on-the-fly-computed achievements — see feature/badge/BadgeCalculator.kt. */
class BadgeAct : BaseAct() {

    private lateinit var binding: ABadgesBinding

    private data class Row(val badgeId: BadgeId, val card: CardView, val name: AppCompatTextView, val desc: AppCompatTextView)

    private lateinit var rows: List<Row>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ABadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.badgeBackBtn.setOnClickListener { finish() }

        rows = listOf(
            Row(BadgeId.STREAK_3, binding.cardStreak3, binding.tvStreak3Name, binding.tvStreak3Desc),
            Row(BadgeId.STREAK_7, binding.cardStreak7, binding.tvStreak7Name, binding.tvStreak7Desc),
            Row(BadgeId.STREAK_30, binding.cardStreak30, binding.tvStreak30Name, binding.tvStreak30Desc),
            Row(BadgeId.PERFECT_QUIZ, binding.cardPerfectQuiz, binding.tvPerfectQuizName, binding.tvPerfectQuizDesc),
            Row(BadgeId.PERFECT_EXAM, binding.cardPerfectExam, binding.tvPerfectExamName, binding.tvPerfectExamDesc),
            Row(BadgeId.FLASHCARD_50, binding.cardFlashcard50, binding.tvFlashcard50Name, binding.tvFlashcard50Desc),
            Row(BadgeId.FLASHCARD_100, binding.cardFlashcard100, binding.tvFlashcard100Name, binding.tvFlashcard100Desc),
        )

        val targetAlphas = renderBadges(computeStats())
        animateEntrance(targetAlphas)
    }

    private fun computeStats(): BadgeStats {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val symbols = elements.map { it.short }

        return BadgeStats(
            currentStreak = StudyStreakPref(this).getCurrentStreak(),
            bestQuizScore = QuizBestScorePref(this).getBestScore(),
            quizTotalQuestions = QuizAct.DEFAULT_QUESTION_COUNT,
            hasPerfectExam = ExamHistoryPref(this).getHistory().any { it.score == it.total },
            flashcardsReviewedCount = FlashcardPref(this).countReviewedSymbols(symbols),
        )
    }

    /** Applies locked/unlocked colors immediately and returns each row's target (final) alpha for the entrance animation. */
    private fun renderBadges(stats: BadgeStats): Map<BadgeId, Float> {
        val unlocked = BadgeCalculator.computeUnlockedBadges(stats)
        val unlockedBg = getColorFromAttr(R.attr.colorPrimaryContainer)
        val unlockedText = getColorFromAttr(R.attr.colorOnPrimaryContainer)
        val lockedBg = getColorFromAttr(R.attr.colorSurfaceVariant)
        val lockedText = getColorFromAttr(R.attr.colorOnSurfaceVariant)

        val targetAlphas = mutableMapOf<BadgeId, Float>()
        for (row in rows) {
            val isUnlocked = row.badgeId in unlocked
            row.card.setCardBackgroundColor(if (isUnlocked) unlockedBg else lockedBg)
            val textColor = if (isUnlocked) unlockedText else lockedText
            row.name.setTextColor(textColor)
            row.desc.setTextColor(textColor)
            targetAlphas[row.badgeId] = if (isUnlocked) 1f else 0.55f
        }
        return targetAlphas
    }

    // Staggered pop-in entrance for the badge rows, matching the option-card entrance motion
    // already used in FlashcardAct/QuizAct (fade + rise + overshoot scale, delayed per index).
    private fun animateEntrance(targetAlphas: Map<BadgeId, Float>) {
        rows.forEachIndexed { index, row ->
            val targetAlpha = targetAlphas[row.badgeId] ?: 1f
            row.card.alpha = 0f
            row.card.translationY = 40f
            row.card.scaleX = 0.9f
            row.card.scaleY = 0.9f
            row.card.animate()
                .alpha(targetAlpha)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(index * 60L)
                .setDuration(350)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }
    }

    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.badgeTitleBar.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.badgeTitleBar.layoutParams = params
        binding.badgeRootView.setPadding(0, 0, 0, bottom)
    }
}
