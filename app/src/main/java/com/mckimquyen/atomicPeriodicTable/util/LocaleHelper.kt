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
        val locale = Locale(languageCode)
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
            LanguagePref.LANG_CHINESE -> "中文"
            else -> "English"
        }
    }
}
