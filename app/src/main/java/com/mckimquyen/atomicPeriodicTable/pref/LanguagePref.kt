package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Preference class to store and retrieve the selected language.
 * Default language is English.
 */
class LanguagePref(context: Context) {
    companion object {
        const val LANG_ENGLISH = "en"
        const val LANG_VIETNAMESE = "vi"
        const val LANG_CHINESE = "zh"
    }

    private val prefName = "Language_Preference"
    private val prefKey = "Language_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): String {
        return preference.getString(prefKey, LANG_ENGLISH) ?: LANG_ENGLISH
    }

    fun setValue(languageCode: String) {
        preference.edit(commit = true) {
            putString(prefKey, languageCode)
        }
    }
}
