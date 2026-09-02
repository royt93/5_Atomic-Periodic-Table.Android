package com.mckimquyen.atomicPeriodicTable.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.pref.FontScalePref
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for the exact bug LocaleHelper.setLocale()'s "FIX (P3)" comment warns
 * about: font-scale handling must MULTIPLY onto the device's own system fontScale, never
 * replace it — replacing would silently wipe the user's system-wide accessibility setting.
 */
@RunWith(AndroidJUnit4::class)
class FontScaleHelperTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun resetToDefault() {
        context.getSharedPreferences("Font_Scale_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun defaultPreference_returnsTheSameContext_noWrapNeeded() {
        val result = FontScaleHelper.applyFontScale(context)
        assertSame(context, result)
    }

    @Test
    fun largePreference_multipliesSystemFontScale_ratherThanReplacingIt() {
        val systemFontScale = context.resources.configuration.fontScale
        FontScalePref(context).setValue(FontScalePref.LARGE)

        val result = FontScaleHelper.applyFontScale(context)

        val expected = systemFontScale * 1.15f
        assertEquals(expected, result.resources.configuration.fontScale, 0.001f)
    }

    @Test
    fun smallPreference_multipliesSystemFontScale_ratherThanReplacingIt() {
        val systemFontScale = context.resources.configuration.fontScale
        FontScalePref(context).setValue(FontScalePref.SMALL)

        val result = FontScaleHelper.applyFontScale(context)

        val expected = systemFontScale * 0.85f
        assertEquals(expected, result.resources.configuration.fontScale, 0.001f)
    }
}
