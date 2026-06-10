package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class NotesPref(context: Context) {
    private val prefName = "Element_Notes_Preference"
    private val preference: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getNote(symbol: String): String {
        val newKey = "notes_$symbol"
        val current = preference.getString(newKey, null)
        if (current != null) return current

        // Migration: legacy notes were stored under the "note_" prefix.
        val legacy = preference.getString("note_$symbol", "") ?: ""
        if (legacy.isNotEmpty()) {
            preference.edit {
                putString(newKey, legacy)
                remove("note_$symbol")
            }
        }
        return legacy
    }

    fun saveNote(symbol: String, note: String) {
        preference.edit {
            putString("notes_$symbol", note)
        }
    }
}
