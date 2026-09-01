package com.mckimquyen.atomicPeriodicTable.feature.flashcard

import kotlin.math.roundToInt

enum class FlashcardRating { AGAIN, HARD, GOOD, EASY }

data class FlashcardState(
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
)

/**
 * Pure/Android-independent (no Context, no SharedPreferences) so the spaced-repetition math
 * is JVM-unit-testable, same convention as VipCalculator. A simplified SM-2: AGAIN resets
 * progress and shortens the interval, HARD/GOOD/EASY grow the interval by easeFactor with
 * different starting steps and ease adjustments.
 */
object FlashcardScheduler {
    const val MIN_EASE_FACTOR = 1.3f
    private const val MS_PER_DAY = 86_400_000L

    fun review(state: FlashcardState, rating: FlashcardRating): FlashcardState = when (rating) {
        FlashcardRating.AGAIN -> FlashcardState(
            easeFactor = (state.easeFactor - 0.2f).coerceAtLeast(MIN_EASE_FACTOR),
            intervalDays = 1,
            repetitions = 0,
        )

        FlashcardRating.HARD -> {
            val repetitions = state.repetitions + 1
            FlashcardState(
                easeFactor = (state.easeFactor - 0.15f).coerceAtLeast(MIN_EASE_FACTOR),
                intervalDays = if (repetitions <= 1) 1 else growInterval(state.intervalDays, 1.2f),
                repetitions = repetitions,
            )
        }

        FlashcardRating.GOOD -> {
            val repetitions = state.repetitions + 1
            val intervalDays = when (repetitions) {
                1 -> 1
                2 -> 6
                else -> growInterval(state.intervalDays, state.easeFactor)
            }
            FlashcardState(easeFactor = state.easeFactor, intervalDays = intervalDays, repetitions = repetitions)
        }

        FlashcardRating.EASY -> {
            val repetitions = state.repetitions + 1
            val intervalDays = when (repetitions) {
                1 -> 4
                2 -> 10
                else -> growInterval(state.intervalDays, state.easeFactor * 1.3f)
            }
            FlashcardState(easeFactor = state.easeFactor + 0.15f, intervalDays = intervalDays, repetitions = repetitions)
        }
    }

    private fun growInterval(currentIntervalDays: Int, multiplier: Float): Int =
        (currentIntervalDays * multiplier).roundToInt().coerceAtLeast(1)

    fun nextReviewAtMs(nowMs: Long, intervalDays: Int): Long = nowMs + intervalDays * MS_PER_DAY

    fun isDue(nextReviewAtMs: Long, nowMs: Long): Boolean = nextReviewAtMs <= nowMs
}
