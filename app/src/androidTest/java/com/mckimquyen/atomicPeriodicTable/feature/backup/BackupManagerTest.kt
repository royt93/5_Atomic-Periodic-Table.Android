package com.mckimquyen.atomicPeriodicTable.feature.backup

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardState
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.pref.NotesPref
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private lateinit var context: android.content.Context

    @Before
    fun clearAllProgressPrefs() {
        context = ApplicationProvider.getApplicationContext()
        for (prefName in listOf("Flashcard_Preference", "Study_Streak_Preference", "Exam_History_Preference", "Element_Notes_Preference")) {
            context.getSharedPreferences(prefName, android.content.Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test
    fun exportThenImport_restoresFlashcardStateExactly() {
        FlashcardPref(context).saveState("H", FlashcardState(easeFactor = 2.1f, intervalDays = 6, repetitions = 3), nextReviewAtMs = 123_456L)

        val json = BackupManager.export(context)
        clearAllProgressPrefs()
        val result = BackupManager.import(context, json)

        assertTrue(result.isSuccess)
        val restored = FlashcardPref(context).getState("H")
        assertEquals(2.1f, restored.easeFactor, 0.001f)
        assertEquals(6, restored.intervalDays)
        assertEquals(3, restored.repetitions)
        assertEquals(123_456L, FlashcardPref(context).getNextReviewAtMs("H"))
    }

    @Test
    fun exportThenImport_restoresStreakExactly() {
        StudyStreakPref(context).recordStudyToday(todayEpochDay = 20005L)

        val json = BackupManager.export(context)
        clearAllProgressPrefs()
        BackupManager.import(context, json)

        assertEquals(1, StudyStreakPref(context).getCurrentStreak())
        assertEquals(20005L, StudyStreakPref(context).getLastEpochDay())
    }

    @Test
    fun exportThenImport_restoresExamHistoryExactly() {
        ExamHistoryPref(context).addResult(score = 18, total = 20, timestampMs = 1000L)
        ExamHistoryPref(context).addResult(score = 20, total = 20, timestampMs = 2000L)

        val json = BackupManager.export(context)
        clearAllProgressPrefs()
        BackupManager.import(context, json)

        val history = ExamHistoryPref(context).getHistory()
        assertEquals(2, history.size)
        assertEquals(20, history[0].score) // most recent first, preserved
        assertEquals(18, history[1].score)
    }

    @Test
    fun exportThenImport_restoresNonEmptyNotesOnly() {
        NotesPref(context).saveNote("H", "Lightest element")

        val json = BackupManager.export(context)
        clearAllProgressPrefs()
        BackupManager.import(context, json)

        assertEquals("Lightest element", NotesPref(context).getNote("H"))
        assertEquals("", NotesPref(context).getNote("He")) // untouched symbol stays empty
    }

    @Test
    fun import_overwritesExistingData_ratherThanMerging() {
        StudyStreakPref(context).recordStudyToday(todayEpochDay = 20000L) // streak = 1 originally
        val json = BackupManager.export(context) // backup captures streak = 1

        // Diverge local state after the backup was taken.
        for (day in 20001L..20009L) StudyStreakPref(context).recordStudyToday(todayEpochDay = day)
        assertEquals(10, StudyStreakPref(context).getCurrentStreak())

        BackupManager.import(context, json)

        assertEquals("import must overwrite, not merge/keep-higher", 1, StudyStreakPref(context).getCurrentStreak())
    }

    @Test
    fun import_rejectsNewerSchemaVersion() {
        val futureJson = """{"schemaVersion":999,"flashcards":[],"currentStreak":0,"lastStreakEpochDay":0,"examHistory":[],"notes":[]}"""

        val result = BackupManager.import(context, futureJson)

        assertTrue(result.isFailure)
    }

    @Test
    fun import_corruptJson_failsGracefully_withoutCrashing() {
        val result = BackupManager.import(context, "{ this is not valid json ]]]")

        assertTrue(result.isFailure)
    }

    @Test
    fun import_emptyString_failsGracefully() {
        val result = BackupManager.import(context, "")

        assertFalse(result.isSuccess)
    }
}
