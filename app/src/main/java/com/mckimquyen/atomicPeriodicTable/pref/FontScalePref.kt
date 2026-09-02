package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class FontScalePref(context: Context) {

    private val preferenceName = "Font_Scale_Preference"
    private val preferenceValue = "Font_Scale_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int = preference.getInt(preferenceValue, DEFAULT)

    fun setValue(value: Int) {
        preference.edit { putInt(preferenceValue, value) }
    }

    companion object {
        const val SMALL = 0
        const val DEFAULT = 1
        const val LARGE = 2
    }
}
