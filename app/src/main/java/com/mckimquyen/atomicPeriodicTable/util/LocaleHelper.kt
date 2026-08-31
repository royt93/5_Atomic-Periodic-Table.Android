package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.mckimquyen.atomicPeriodicTable.pref.LanguagePref
import java.util.Locale
import android.util.Log

/**
 * Helper object to manage app locale/language settings.
 * Call applyLanguage() in attachBaseContext() of all Activities.
 */
object LocaleHelper {

    /**
     * Apply the saved language preference to the context.
     * Returns a new context with the applied locale.
     */
    fun applyLanguage(context: Context): Context {
        val languagePref = LanguagePref(context)
        val languageCode = languagePref.getValue()
        Log.i("LocaleHelper", "applyLanguage: fetching lang=$languageCode")
        return setLocale(context, languageCode)
    }

    /**
     * Set the locale for the given context.
     * Returns a new context with the applied locale configuration.
     */
    fun setLocale(context: Context, languageCode: String): Context {
        Log.i("LocaleHelper", "setLocale: setting to $languageCode")
        val locale = when (languageCode) {
            "in", "id" -> Locale("in")
            "zh-rTW", "zh-TW" -> Locale("zh", "TW")
            "pt-rBR", "pt-BR" -> Locale("pt", "BR")
            else -> Locale.forLanguageTag(languageCode.replace("-r", "-"))
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)
        config.fontScale = 1.0f

        return context.createConfigurationContext(config)
    }

    /**
     * Get the display name of a language code.
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LanguagePref.LANG_ENGLISH -> "English"
            LanguagePref.LANG_VIETNAMESE -> "Tiếng Việt"
            LanguagePref.LANG_CHINESE -> "中文 (简体)"
            LanguagePref.LANG_CHINESE_TRADITIONAL -> "中文 (繁體)"
            LanguagePref.LANG_FRENCH -> "Français"
            LanguagePref.LANG_GERMAN -> "Deutsch"
            LanguagePref.LANG_JAPANESE -> "日本語"
            LanguagePref.LANG_KOREAN -> "한국어"
            LanguagePref.LANG_SPANISH -> "Español"
            LanguagePref.LANG_RUSSIAN -> "Русский"
            LanguagePref.LANG_THAI -> "ไทย"
            LanguagePref.LANG_ARABIC -> "العربية"
            LanguagePref.LANG_PORTUGUESE -> "Português"
            LanguagePref.LANG_PORTUGUESE_BRAZIL -> "Português (Brasil)"
            LanguagePref.LANG_HINDI -> "हिन्दी"
            LanguagePref.LANG_ITALIAN -> "Italiano"
            LanguagePref.LANG_INDONESIAN -> "Bahasa Indonesia"
            else -> "English"
        }
    }
}
