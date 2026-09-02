package com.mckimquyen.atomicPeriodicTable.feature.history

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Structured list (most-recently-viewed symbol first) → Gson, same convention as
 * ExamHistoryPref/BackupData (flat SharedPreferences keys don't fit an ordered list).
 */
class RecentlyViewedPref(context: Context) {
    private val preference = context.getSharedPreferences("Recently_Viewed_Preference", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    fun getRecent(): List<String> {
        val json = preference.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson<List<String>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Moves [symbol] to the front if already present (no duplicates), caps at MAX_RECENT. */
    fun recordViewed(symbol: String) {
        val updated = withRecorded(getRecent(), symbol)
        preference.edit { putString(KEY, gson.toJson(updated)) }
    }

    companion object {
        private const val KEY = "recently_viewed_symbols_json"
        const val MAX_RECENT = 10

        /** Pure/Android-independent so the add/dedupe/cap logic is JVM-testable without a Context. */
        fun withRecorded(current: List<String>, symbol: String, maxRecent: Int = MAX_RECENT): List<String> =
            (listOf(symbol) + current.filterNot { it == symbol }).take(maxRecent)
    }
}
