package com.mckimquyen.atomicPeriodicTable.feature.trivia

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class DailyTriviaPref(context: Context) {
    private val preference: SharedPreferences =
        context.getSharedPreferences("Daily_Trivia_Preference", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = preference.getBoolean("enabled", false)

    fun setEnabled(enabled: Boolean) {
        preference.edit { putBoolean("enabled", enabled) }
    }
}
