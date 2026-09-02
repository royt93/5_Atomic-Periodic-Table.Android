package com.mckimquyen.atomicPeriodicTable.feature.exam

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ExamResult(val score: Int, val total: Int, val timestampMs: Long)

/**
 * Unlike other Pref classes in this app (flat SharedPreferences key per field), exam history is
 * a structured list — Gson-serialized into a single key, per the convention agreed in
 * doc/task/feat_new.md mục 8.
 */
class ExamHistoryPref(context: Context) {
    private val preference = context.getSharedPreferences("Exam_History_Preference", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val historyType = object : TypeToken<List<ExamResult>>() {}.type

    fun addResult(score: Int, total: Int, timestampMs: Long = System.currentTimeMillis()) {
        val updated = listOf(ExamResult(score, total, timestampMs)) + getHistory()
        preference.edit { putString(KEY_HISTORY, gson.toJson(updated.take(MAX_HISTORY))) }
    }

    /** Overwrites the whole history verbatim (no prepend/cap logic) — for restoring from a backup. */
    fun replaceHistory(history: List<ExamResult>) {
        preference.edit { putString(KEY_HISTORY, gson.toJson(history.take(MAX_HISTORY))) }
    }

    fun getHistory(): List<ExamResult> {
        val json = preference.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            gson.fromJson<List<ExamResult>>(json, historyType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_HISTORY = "exam_history_json"
        private const val MAX_HISTORY = 20
    }
}
