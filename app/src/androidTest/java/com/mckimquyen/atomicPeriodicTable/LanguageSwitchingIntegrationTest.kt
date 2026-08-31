package com.mckimquyen.atomicPeriodicTable

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.pref.LanguagePref
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.LocaleHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageSwitchingIntegrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var languagePref: LanguagePref
    private var originalLang: String = LanguagePref.LANG_ENGLISH

    @Before
    fun setup() {
        languagePref = LanguagePref(context)
        originalLang = languagePref.getValue()
    }

    @After
    fun tearDown() {
        languagePref.setValue(originalLang)
        LocaleHelper.setLocale(context, originalLang)
    }

    @Test
    fun testLanguagePersistenceAndLocalePropagation() {
        val testLanguages = listOf(
            LanguagePref.LANG_VIETNAMESE to "Hydro",
            LanguagePref.LANG_FRENCH to "Hydrogène",
            LanguagePref.LANG_GERMAN to "Wasserstoff",
            LanguagePref.LANG_SPANISH to "Hidrógeno",
            LanguagePref.LANG_RUSSIAN to "Водород",
            LanguagePref.LANG_JAPANESE to "水素",
            LanguagePref.LANG_CHINESE to "氢",
            LanguagePref.LANG_ENGLISH to "Hydrogen"
        )

        for ((langCode, expectedHydrogen) in testLanguages) {
            // Save preference
            languagePref.setValue(langCode)
            assertEquals(langCode, languagePref.getValue())

            // Apply language to context
            val localizedContext = LocaleHelper.applyLanguage(context)
            assertNotNull(localizedContext)

            // Resolve localized element name
            val hydrogenName = ElementTranslator.getLocalizedName(localizedContext, "Hydrogen")
            assertEquals(
                "Hydrogen name in $langCode should match expected translation",
                expectedHydrogen,
                hydrogenName
            )
        }
    }

    @Test
    fun testAllLanguagesHaveValidDisplayNames() {
        val allCodes = listOf(
            LanguagePref.LANG_ENGLISH,
            LanguagePref.LANG_VIETNAMESE,
            LanguagePref.LANG_CHINESE,
            LanguagePref.LANG_CHINESE_TRADITIONAL,
            LanguagePref.LANG_FRENCH,
            LanguagePref.LANG_GERMAN,
            LanguagePref.LANG_JAPANESE,
            LanguagePref.LANG_KOREAN,
            LanguagePref.LANG_SPANISH,
            LanguagePref.LANG_RUSSIAN,
            LanguagePref.LANG_THAI,
            LanguagePref.LANG_ARABIC,
            LanguagePref.LANG_PORTUGUESE,
            LanguagePref.LANG_PORTUGUESE_BRAZIL,
            LanguagePref.LANG_HINDI,
            LanguagePref.LANG_ITALIAN,
            LanguagePref.LANG_INDONESIAN
        )

        for (code in allCodes) {
            val displayName = LocaleHelper.getLanguageDisplayName(code)
            assertNotNull(displayName)
            org.junit.Assert.assertTrue(
                "Display name for $code should not be blank",
                displayName.isNotBlank()
            )
        }
    }
}
