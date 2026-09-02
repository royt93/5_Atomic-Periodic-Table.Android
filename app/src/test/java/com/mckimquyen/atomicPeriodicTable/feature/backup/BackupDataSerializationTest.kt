package com.mckimquyen.atomicPeriodicTable.feature.backup

import com.google.gson.Gson
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamResult
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Exercises the exact Gson shape BackupManager relies on, without an Android Context
 * (SharedPreferences round-trip itself is covered by the instrumented BackupManagerTest).
 */
class BackupDataSerializationTest {

    private val gson = Gson()

    @Test
    fun roundTrip_fullData_preservesEveryField() {
        val original = BackupData(
            schemaVersion = 1,
            flashcards = listOf(
                FlashcardEntry("H", FlashcardState(easeFactor = 2.3f, intervalDays = 4, repetitions = 2), nextReviewAtMs = 111L),
                FlashcardEntry("He", FlashcardState(), nextReviewAtMs = 0L),
            ),
            currentStreak = 7,
            lastStreakEpochDay = 20000L,
            examHistory = listOf(ExamResult(score = 18, total = 20, timestampMs = 999L)),
            notes = listOf(NoteEntry("H", "Lightest element")),
        )

        val json = gson.toJson(original)
        val decoded = gson.fromJson(json, BackupData::class.java)

        assertEquals(original, decoded)
    }

    @Test
    fun roundTrip_emptyCollections() {
        val original = BackupData(schemaVersion = 1)
        val json = gson.toJson(original)
        val decoded = gson.fromJson(json, BackupData::class.java)

        assertEquals(original, decoded)
    }

    @Test
    fun defaultSchemaVersion_isCurrentVersion() {
        assertEquals(BackupData.CURRENT_SCHEMA_VERSION, BackupData().schemaVersion)
    }
}
