package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class DictionaryPref(context: Context) {

    private val prefName = "Dictionary_Preference"
    private val prefValue = "Dictionary_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): String {
        return preference.getString(prefValue, "chemistry") ?: ""

    }

    fun setValue(string: String) {
        preference.edit {
            putString(prefValue, string)
        }
    }
}
