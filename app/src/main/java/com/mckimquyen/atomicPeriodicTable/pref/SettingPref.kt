package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class OfflinePreference(context: Context) {
    private val prefName = "Offline_Preference"
    private val prefValue = "Offline_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(prefValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(prefValue, count)
        }
    }
}
