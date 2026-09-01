package com.mckimquyen.atomicPeriodicTable.feature.flashcard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashcardSchedulerTest {

    private val fresh = FlashcardState()

    @Test
    fun again_fromFreshCard_resetsToOneDayAndLowersEase() {
        val result = FlashcardScheduler.review(fresh, FlashcardRating.AGAIN)
        assertEquals(1, result.intervalDays)
        assertEquals(0, result.repetitions)
        assertEquals(2.3f, result.easeFactor, 0.001f)
    }

    @Test
    fun again_neverDropsEaseBelowMinimum() {
        val lowEase = FlashcardState(easeFactor = 1.35f, intervalDays = 10, repetitions = 5)
        val result = FlashcardScheduler.review(lowEase, FlashcardRating.AGAIN)
        assertEquals(FlashcardScheduler.MIN_EASE_FACTOR, result.easeFactor, 0.001f)
    }

    @Test
    fun again_resetsProgressEvenAfterManyRepetitions() {
        val mature = FlashcardState(easeFactor = 2.8f, intervalDays = 60, repetitions = 8)
        val result = FlashcardScheduler.review(mature, FlashcardRating.AGAIN)
        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun hard_firstRepetition_isOneDay() {
        val result = FlashcardScheduler.review(fresh, FlashcardRating.HARD)
        assertEquals(1, result.repetitions)
        assertEquals(1, result.intervalDays)
        assertEquals(2.35f, result.easeFactor, 0.001f)
    }

    @Test
    fun hard_afterFirstRepetition_growsByOnePointTwo() {
        val state = FlashcardState(easeFactor = 2.5f, intervalDays = 10, repetitions = 1)
        val result = FlashcardScheduler.review(state, FlashcardRating.HARD)
        assertEquals(2, result.repetitions)
        assertEquals(12, result.intervalDays) // round(10 * 1.2) = 12
    }

    @Test
    fun hard_neverDropsEaseBelowMinimum() {
        val lowEase = FlashcardState(easeFactor = 1.3f, intervalDays = 5, repetitions = 3)
        val result = FlashcardScheduler.review(lowEase, FlashcardRating.HARD)
        assertEquals(FlashcardScheduler.MIN_EASE_FACTOR, result.easeFactor, 0.001f)
    }

    @Test
    fun good_firstRepetition_isOneDay() {
        val result = FlashcardScheduler.review(fresh, FlashcardRating.GOOD)
        assertEquals(1, result.repetitions)
        assertEquals(1, result.intervalDays)
        assertEquals(2.5f, result.easeFactor, 0.001f) // GOOD does not change ease
    }

    @Test
    fun good_secondRepetition_isSixDays() {
        val state = FlashcardState(easeFactor = 2.5f, intervalDays = 1, repetitions = 1)
        val result = FlashcardScheduler.review(state, FlashcardRating.GOOD)
        assertEquals(2, result.repetitions)
        assertEquals(6, result.intervalDays)
    }

    @Test
    fun good_thirdAndLaterRepetition_growsByEaseFactor() {
        val state = FlashcardState(easeFactor = 2.5f, intervalDays = 6, repetitions = 2)
        val result = FlashcardScheduler.review(state, FlashcardRating.GOOD)
        assertEquals(3, result.repetitions)
        assertEquals(15, result.intervalDays) // round(6 * 2.5) = 15
    }

    @Test
    fun easy_firstRepetition_isFourDays() {
        val result = FlashcardScheduler.review(fresh, FlashcardRating.EASY)
        assertEquals(1, result.repetitions)
        assertEquals(4, result.intervalDays)
        assertEquals(2.65f, result.easeFactor, 0.001f)
    }

    @Test
    fun easy_secondRepetition_isTenDays() {
        val state = FlashcardState(easeFactor = 2.5f, intervalDays = 4, repetitions = 1)
        val result = FlashcardScheduler.review(state, FlashcardRating.EASY)
        assertEquals(2, result.repetitions)
        assertEquals(10, result.intervalDays)
    }

    @Test
    fun easy_thirdAndLaterRepetition_growsFasterThanGood() {
        val state = FlashcardState(easeFactor = 2.5f, intervalDays = 10, repetitions = 2)
        val easyResult = FlashcardScheduler.review(state, FlashcardRating.EASY)
        val goodResult = FlashcardScheduler.review(state, FlashcardRating.GOOD)
        assertEquals(3, easyResult.repetitions)
        assertTrue(
            "EASY interval (${easyResult.intervalDays}) should grow faster than GOOD (${goodResult.intervalDays})",
            easyResult.intervalDays > goodResult.intervalDays,
        )
    }

    @Test
    fun intervalNeverGoesBelowOneDay_evenForTinyEaseFactor() {
        val state = FlashcardState(easeFactor = FlashcardScheduler.MIN_EASE_FACTOR, intervalDays = 0, repetitions = 2)
        val result = FlashcardScheduler.review(state, FlashcardRating.GOOD)
        assertTrue(result.intervalDays >= 1)
    }

    @Test
    fun nextReviewAtMs_addsIntervalDaysInMilliseconds() {
        val now = 1_000_000L
        assertEquals(now + 3 * 86_400_000L, FlashcardScheduler.nextReviewAtMs(now, 3))
    }

    @Test
    fun isDue_trueWhenNextReviewInThePastOrNow() {
        assertTrue(FlashcardScheduler.isDue(nextReviewAtMs = 100L, nowMs = 100L))
        assertTrue(FlashcardScheduler.isDue(nextReviewAtMs = 50L, nowMs = 100L))
    }

    @Test
    fun isDue_falseWhenNextReviewInTheFuture() {
        assertTrue(!FlashcardScheduler.isDue(nextReviewAtMs = 200L, nowMs = 100L))
    }
}
