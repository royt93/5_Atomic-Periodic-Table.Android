package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class NotesPref(context: Context) {
    private val prefName = "Element_Notes_Preference"
    private val preference: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getNote(symbol: String): String {
        return preference.getString("note_$symbol", "") ?: ""
    }

    fun saveNote(symbol: String, note: String) {
        preference.edit {
            putString("note_$symbol", note)
        }
    }
}
