package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class IsoPref(context: Context) {

    private val prefName = "Iso_Preference"
    private val prefValue = "Iso_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(prefValue, 0)
        //0 == Alphabetical
        //1 == Element Number
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(prefValue, count)
        }
    }
}

class SendIso(context: Context) {

    private val prefName = "send_Iso_pref"
    private val prefValue = "send_iso_value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): String {
        // Optimized: Use elvis operator instead of non-null assertion to avoid potential crash
        return preference.getString(prefValue, "false") ?: "false"
        //0 == Not sent
        //1 == Sent
    }

    fun setValue(count: String) {
        preference.edit {
            putString(prefValue, count)
        }
    }
}
