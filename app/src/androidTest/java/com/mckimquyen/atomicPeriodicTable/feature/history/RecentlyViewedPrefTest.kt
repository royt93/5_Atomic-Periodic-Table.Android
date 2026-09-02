package com.mckimquyen.atomicPeriodicTable.feature.history

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentlyViewedPrefTest {

    private lateinit var pref: RecentlyViewedPref

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Recently_Viewed_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        pref = RecentlyViewedPref(context)
    }

    @Test
    fun noHistoryYet_isEmpty() {
        assertTrue(pref.getRecent().isEmpty())
    }

    @Test
    fun recordViewed_persistsAcrossInstances() {
        pref.recordViewed("H")
        pref.recordViewed("He")

        val freshInstance = RecentlyViewedPref(ApplicationProvider.getApplicationContext())
        assertEquals(listOf("He", "H"), freshInstance.getRecent())
    }

    @Test
    fun recordViewed_capsAtMaxRecent() {
        repeat(RecentlyViewedPref.MAX_RECENT + 5) { i -> pref.recordViewed("E$i") }
        assertEquals(RecentlyViewedPref.MAX_RECENT, pref.getRecent().size)
        assertEquals("E${RecentlyViewedPref.MAX_RECENT + 4}", pref.getRecent().first())
    }
}
