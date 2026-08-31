package com.mckimquyen.atomicPeriodicTable.widget

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiLanguageWidgetTest {

    private val baseContext: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testElementTranslatorWidget_vietnamese() {
        val viContext = LocaleHelper.setLocale(baseContext, "vi")
        
        val hName = ElementTranslator.getLocalizedName(viContext, "Hydrogen")
        val heName = ElementTranslator.getLocalizedName(viContext, "Helium")
        val feName = ElementTranslator.getLocalizedName(viContext, "Iron")
        val auName = ElementTranslator.getLocalizedName(viContext, "Gold")

        assertEquals("Hydro", hName)
        assertEquals("Heli", heName)
        assertEquals("Sắt", feName)
        assertEquals("Vàng", auName)
    }

    @Test
    fun testElementTranslatorWidget_french() {
        val frContext = LocaleHelper.setLocale(baseContext, "fr")
        
        val hName = ElementTranslator.getLocalizedName(frContext, "Hydrogen")
        val feName = ElementTranslator.getLocalizedName(frContext, "Iron")
        val auName = ElementTranslator.getLocalizedName(frContext, "Gold")

        assertEquals("Hydrogène", hName)
        assertEquals("Fer", feName)
        assertEquals("Or", auName)
    }

    @Test
    fun testElementTranslatorWidget_german() {
        val deContext = LocaleHelper.setLocale(baseContext, "de")
        
        val hName = ElementTranslator.getLocalizedName(deContext, "Hydrogen")
        val feName = ElementTranslator.getLocalizedName(deContext, "Iron")
        val oName = ElementTranslator.getLocalizedName(deContext, "Oxygen")

        assertEquals("Wasserstoff", hName)
        assertEquals("Eisen", feName)
        assertEquals("Sauerstoff", oName)
    }

    @Test
    fun testElementTranslatorWidget_spanish() {
        val esContext = LocaleHelper.setLocale(baseContext, "es")
        
        val hName = ElementTranslator.getLocalizedName(esContext, "Hydrogen")
        val feName = ElementTranslator.getLocalizedName(esContext, "Iron")
        val cuName = ElementTranslator.getLocalizedName(esContext, "Copper")

        assertEquals("Hidrógeno", hName)
        assertEquals("Hierro", feName)
        assertEquals("Cobre", cuName)
    }

    @Test
    fun testElementTranslatorWidget_japanese() {
        val jaContext = LocaleHelper.setLocale(baseContext, "ja")
        
        val hName = ElementTranslator.getLocalizedName(jaContext, "Hydrogen")
        val feName = ElementTranslator.getLocalizedName(jaContext, "Iron")
        val auName = ElementTranslator.getLocalizedName(jaContext, "Gold")

        assertEquals("水素", hName)
        assertEquals("鉄", feName)
        assertEquals("金", auName)
    }

    @Test
    fun testElementTranslatorWidget_chinese() {
        val zhContext = LocaleHelper.setLocale(baseContext, "zh")
        
        val hName = ElementTranslator.getLocalizedName(zhContext, "Hydrogen")
        val feName = ElementTranslator.getLocalizedName(zhContext, "Iron")
        val auName = ElementTranslator.getLocalizedName(zhContext, "Gold")

        assertEquals("氢", hName)
        assertEquals("铁", feName)
        assertEquals("金", auName)
    }

    @Test
    fun testCategoryTranslatorWidget_multipleLocales() {
        val viContext = LocaleHelper.setLocale(baseContext, "vi")
        val viCat = CategoryTranslator.translate(viContext, "alkali metal")
        assertEquals(viContext.getString(R.string.cat_alkali_metals), viCat)

        val frContext = LocaleHelper.setLocale(baseContext, "fr")
        val frCat = CategoryTranslator.translate(frContext, "noble gases")
        assertEquals(frContext.getString(R.string.cat_noble_gases), frCat)

        val deContext = LocaleHelper.setLocale(baseContext, "de")
        val deCat = CategoryTranslator.translate(deContext, "transition metal")
        assertEquals(deContext.getString(R.string.cat_transition_metals), deCat)
    }

    @Test
    fun testIonViewAllStringFormatting_multipleLocales() {
        val locales = listOf("en", "vi", "fr", "de", "es", "ja", "zh", "ru")
        for (lang in locales) {
            val ctx = LocaleHelper.setLocale(baseContext, lang)
            val formatted = ctx.getString(R.string.ion_view_all, 5)
            assertTrue("Formatted string in $lang should contain '5'", formatted.contains("5"))
        }
    }
}
