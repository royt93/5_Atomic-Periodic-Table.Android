package com.mckimquyen.atomicPeriodicTable.feature.flashcard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashcardPrefTest {

    private fun freshPref(): FlashcardPref {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Flashcard_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        return FlashcardPref(context)
    }

    @Test
    fun getState_defaultsToFreshCard_whenNeverSaved() {
        val pref = freshPref()
        val state = pref.getState("Xx")
        assertEquals(FlashcardState(), state)
        assertEquals(0L, pref.getNextReviewAtMs("Xx"))
    }

    @Test
    fun saveState_thenGetState_roundTripsExactly() {
        val pref = freshPref()
        val state = FlashcardState(easeFactor = 2.35f, intervalDays = 6, repetitions = 2)
        pref.saveState("Au", state, nextReviewAtMs = 123_456_789L)

        assertEquals(state, pref.getState("Au"))
        assertEquals(123_456_789L, pref.getNextReviewAtMs("Au"))
    }

    @Test
    fun saveState_isIsolatedPerSymbol() {
        val pref = freshPref()
        pref.saveState("H", FlashcardState(intervalDays = 1, repetitions = 1), 1_000L)
        pref.saveState("He", FlashcardState(intervalDays = 4, repetitions = 1), 2_000L)

        assertEquals(1, pref.getState("H").intervalDays)
        assertEquals(4, pref.getState("He").intervalDays)
        assertEquals(1_000L, pref.getNextReviewAtMs("H"))
        assertEquals(2_000L, pref.getNextReviewAtMs("He"))
    }

    @Test
    fun countReviewedSymbols_countsOnlySymbolsWithAtLeastOneRepetition() {
        val pref = freshPref()
        pref.saveState("H", FlashcardState(repetitions = 1), 1_000L)
        pref.saveState("He", FlashcardState(repetitions = 3), 2_000L)
        pref.saveState("Li", FlashcardState(repetitions = 0), 3_000L) // rated "Again" — never advanced past 0

        assertEquals(2, pref.countReviewedSymbols(listOf("H", "He", "Li", "Be")))
    }

    @Test
    fun countReviewedSymbols_emptySymbolList_isZero() {
        val pref = freshPref()
        assertEquals(0, pref.countReviewedSymbols(emptyList()))
    }

    @Test
    fun countReviewedSymbols_noneReviewedYet_isZero() {
        val pref = freshPref()
        assertEquals(0, pref.countReviewedSymbols(listOf("H", "He", "Li")))
    }
}
