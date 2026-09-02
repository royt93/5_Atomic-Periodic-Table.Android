package com.mckimquyen.atomicPeriodicTable.feature.quiz

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizBestScorePrefTest {

    private lateinit var pref: QuizBestScorePref

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Quiz_Best_Score_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        pref = QuizBestScorePref(context)
    }

    @Test
    fun defaultBestScore_isZero() {
        assertEquals(0, pref.getBestScore())
    }

    @Test
    fun recordScore_higherThanCurrent_updatesBest() {
        pref.recordScore(6)
        assertEquals(6, pref.getBestScore())
        pref.recordScore(9)
        assertEquals(9, pref.getBestScore())
    }

    @Test
    fun recordScore_lowerThanCurrent_doesNotRegressBest() {
        pref.recordScore(9)
        pref.recordScore(4)
        assertEquals(9, pref.getBestScore())
    }

    @Test
    fun recordScore_equalToCurrent_staysUnchanged() {
        pref.recordScore(7)
        pref.recordScore(7)
        assertEquals(7, pref.getBestScore())
    }
}
