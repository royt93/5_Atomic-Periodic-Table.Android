package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.util.Log
import com.mckimquyen.atomicPeriodicTable.BuildConfig

/**
 * Preference class to store and retrieve the selected language.
 * Default language is English.
 */
class LanguagePref(context: Context) {
    companion object {
        const val LANG_ENGLISH = "en"
        const val LANG_VIETNAMESE = "vi"
        const val LANG_CHINESE = "zh"
        // Phase 3
        const val LANG_FRENCH = "fr"
        const val LANG_GERMAN = "de"
        const val LANG_JAPANESE = "ja"
        const val LANG_KOREAN = "ko"
        const val LANG_SPANISH = "es"
        const val LANG_RUSSIAN = "ru"
        const val LANG_THAI = "th"
        // Phase 4
        const val LANG_ARABIC = "ar"
        const val LANG_PORTUGUESE = "pt"
        const val LANG_PORTUGUESE_BRAZIL = "pt-rBR"
        const val LANG_HINDI = "hi"
        const val LANG_CHINESE_TRADITIONAL = "zh-rTW"
        const val LANG_ITALIAN = "it"
        const val LANG_INDONESIAN = "in"
    }

    private val prefName = "Language_Preference"
    private val prefKey = "Language_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getValue(): String {
        val value = preference.getString(prefKey, LANG_ENGLISH) ?: LANG_ENGLISH
        if (BuildConfig.DEBUG) Log.i("LanguagePref", "getValue: $value")
        return value
    }

    fun setValue(languageCode: String) {
        if (BuildConfig.DEBUG) Log.i("LanguagePref", "setValue: saving $languageCode")
        preference.edit(commit = true) {
            putString(prefKey, languageCode)
        }
    }
}
