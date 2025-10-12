package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class ThemePref(context: Context) {

    private val preferenceName = "Theme_Preference"
    private val preferenceValue = "Theme_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 100)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}
