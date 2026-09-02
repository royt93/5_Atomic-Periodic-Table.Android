package com.mckimquyen.atomicPeriodicTable.feature.exam

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExamHistoryPrefTest {

    private lateinit var pref: ExamHistoryPref

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Exam_History_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        pref = ExamHistoryPref(context)
    }

    @Test
    fun noHistoryYet_isEmpty() {
        assertTrue(pref.getHistory().isEmpty())
    }

    @Test
    fun addResult_appearsInHistory() {
        pref.addResult(18, 20, timestampMs = 1000L)
        val history = pref.getHistory()
        assertEquals(1, history.size)
        assertEquals(ExamResult(18, 20, 1000L), history[0])
    }

    @Test
    fun addingMultipleResults_mostRecentFirst() {
        pref.addResult(10, 20, timestampMs = 1000L)
        pref.addResult(15, 20, timestampMs = 2000L)
        pref.addResult(20, 20, timestampMs = 3000L)

        val history = pref.getHistory()
        assertEquals(3, history.size)
        assertEquals(20, history[0].score)
        assertEquals(15, history[1].score)
        assertEquals(10, history[2].score)
    }

    @Test
    fun history_isCappedAtTwentyEntries() {
        repeat(25) { i -> pref.addResult(i, 20, timestampMs = i.toLong()) }
        val history = pref.getHistory()
        assertEquals(20, history.size)
        // Most recent (i=24) must survive; oldest 5 (i=0..4) must be dropped.
        assertEquals(24, history.first().score)
        assertTrue(history.none { it.score < 5 })
    }
}
