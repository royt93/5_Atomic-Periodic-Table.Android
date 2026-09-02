package com.mckimquyen.atomicPeriodicTable.feature.flashcard

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Per-symbol spaced-repetition state, same flat-key-per-symbol pattern as NotesPref —
 * no Map/JSON needed for 118 elements.
 */
class FlashcardPref(context: Context) {
    private val prefName = "Flashcard_Preference"
    private val preference: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getState(symbol: String): FlashcardState = FlashcardState(
        easeFactor = preference.getFloat("flashcard_ease_$symbol", 2.5f),
        intervalDays = preference.getInt("flashcard_interval_$symbol", 0),
        repetitions = preference.getInt("flashcard_reps_$symbol", 0),
    )

    fun getNextReviewAtMs(symbol: String): Long = preference.getLong("flashcard_next_$symbol", 0L)

    /** Count of symbols that have been rated at least once (repetitions > 0), for badge progress. */
    fun countReviewedSymbols(allSymbols: List<String>): Int = allSymbols.count { getState(it).repetitions > 0 }

    fun saveState(symbol: String, state: FlashcardState, nextReviewAtMs: Long) {
        preference.edit {
            putFloat("flashcard_ease_$symbol", state.easeFactor)
            putInt("flashcard_interval_$symbol", state.intervalDays)
            putInt("flashcard_reps_$symbol", state.repetitions)
            putLong("flashcard_next_$symbol", nextReviewAtMs)
        }
    }

    /** Wipes all per-symbol scheduling state — this file holds nothing else, so a full clear is safe. */
    fun clear() {
        preference.edit { clear() }
    }
}
