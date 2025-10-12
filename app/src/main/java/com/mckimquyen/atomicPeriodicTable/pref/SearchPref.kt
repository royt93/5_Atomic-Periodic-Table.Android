package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SearchPref(context: Context) {

    private val prefName = "Search_Preference"
    private val prefValue = "Search_Value"

    private val preference: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(prefValue, 0)
        //0 == Element Number
        //1 == Electronegativity
        //2 == STP Phase
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(prefValue, count)
        }
    }
}
