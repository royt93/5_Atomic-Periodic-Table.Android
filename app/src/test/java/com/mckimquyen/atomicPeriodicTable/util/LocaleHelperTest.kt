package com.mckimquyen.atomicPeriodicTable.util

import com.mckimquyen.atomicPeriodicTable.pref.LanguagePref
import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun testGetLanguageDisplayName() {
        // Test standard languages
        assertEquals("English", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_ENGLISH))
        assertEquals("Tiếng Việt", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_VIETNAMESE))
        assertEquals("中文 (简体)", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_CHINESE))
        
        // Test newly added languages
        assertEquals("Français", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_FRENCH))
        assertEquals("Deutsch", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_GERMAN))
        assertEquals("日本語", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_JAPANESE))
        assertEquals("한국어", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_KOREAN))
        assertEquals("Español", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_SPANISH))
        assertEquals("Русский", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_RUSSIAN))
        assertEquals("ไทย", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_THAI))
        assertEquals("العربية", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_ARABIC))
        assertEquals("Português", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_PORTUGUESE))
        assertEquals("Português (Brasil)", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_PORTUGUESE_BRAZIL))
        assertEquals("हिन्दी", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_HINDI))
        assertEquals("中文 (繁體)", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_CHINESE_TRADITIONAL))
        assertEquals("Italiano", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_ITALIAN))
        assertEquals("Bahasa Indonesia", LocaleHelper.getLanguageDisplayName(LanguagePref.LANG_INDONESIAN))

        // Test fallback language
        assertEquals("English", LocaleHelper.getLanguageDisplayName("unknown_code"))
    }
}
