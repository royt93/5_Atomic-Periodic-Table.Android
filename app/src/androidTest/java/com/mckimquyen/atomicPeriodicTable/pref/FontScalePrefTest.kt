package com.mckimquyen.atomicPeriodicTable.pref

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FontScalePrefTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun clear() {
        context.getSharedPreferences("Font_Scale_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun noValueSetYet_defaultsToDefault() {
        assertEquals(FontScalePref.DEFAULT, FontScalePref(context).getValue())
    }

    @Test
    fun savedValue_persistsAcrossInstances() {
        FontScalePref(context).setValue(FontScalePref.LARGE)
        assertEquals(FontScalePref.LARGE, FontScalePref(context).getValue())
    }
}
