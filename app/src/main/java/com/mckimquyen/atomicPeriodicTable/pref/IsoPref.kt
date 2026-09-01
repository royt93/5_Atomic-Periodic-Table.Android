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
    // FIX (P3): was storing this boolean flag as the String "true"/"false" via
    // getString/putString instead of the dedicated getBoolean/putBoolean API. New key name
    // so an existing install's old String-typed value can't crash getBoolean() with a
    // ClassCastException on first read after updating.
    private val prefValue = "send_iso_value_bool"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): Boolean = preference.getBoolean(prefValue, false)

    fun setValue(sent: Boolean) {
        preference.edit {
            putBoolean(prefValue, sent)
        }
    }
}
