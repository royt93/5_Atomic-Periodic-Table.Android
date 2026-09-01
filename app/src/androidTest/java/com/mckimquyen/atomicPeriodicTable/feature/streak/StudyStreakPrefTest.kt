package com.mckimquyen.atomicPeriodicTable.feature.streak

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyStreakPrefTest {

    private lateinit var pref: StudyStreakPref

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        pref = StudyStreakPref(context)
    }

    @Test
    fun defaultStreak_isZero() {
        assertEquals(0, pref.getCurrentStreak())
    }

    @Test
    fun firstRecord_setsStreakToOne() {
        assertEquals(1, pref.recordStudyToday(todayEpochDay = 20000L))
        assertEquals(1, pref.getCurrentStreak())
    }

    @Test
    fun consecutiveDays_incrementStreak() {
        pref.recordStudyToday(todayEpochDay = 20000L)
        pref.recordStudyToday(todayEpochDay = 20001L)
        assertEquals(3, pref.recordStudyToday(todayEpochDay = 20002L))
    }

    @Test
    fun recordingTwiceSameDay_doesNotDoubleCount() {
        pref.recordStudyToday(todayEpochDay = 20000L)
        assertEquals(1, pref.recordStudyToday(todayEpochDay = 20000L))
    }

    @Test
    fun gapInDays_resetsStreak() {
        pref.recordStudyToday(todayEpochDay = 20000L)
        pref.recordStudyToday(todayEpochDay = 20001L)
        assertEquals(1, pref.recordStudyToday(todayEpochDay = 20010L))
    }
}
