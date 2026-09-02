package com.mckimquyen.atomicPeriodicTable.feature.trivia

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyTriviaPrefTest {

    private lateinit var pref: DailyTriviaPref

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Daily_Trivia_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        pref = DailyTriviaPref(context)
    }

    @Test
    fun defaultState_isDisabled() {
        assertFalse(pref.isEnabled())
    }

    @Test
    fun setEnabledTrue_persists() {
        pref.setEnabled(true)
        assertTrue(pref.isEnabled())
    }

    @Test
    fun setEnabledFalse_afterTrue_persists() {
        pref.setEnabled(true)
        pref.setEnabled(false)
        assertFalse(pref.isEnabled())
    }
}
