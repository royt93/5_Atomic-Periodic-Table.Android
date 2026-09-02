package com.mckimquyen.atomicPeriodicTable.feature.backup

import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamResult
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardState

data class FlashcardEntry(val symbol: String, val state: FlashcardState, val nextReviewAtMs: Long)

data class NoteEntry(val symbol: String, val note: String)

data class BackupData(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val flashcards: List<FlashcardEntry> = emptyList(),
    val currentStreak: Int = 0,
    val lastStreakEpochDay: Long = 0L,
    val examHistory: List<ExamResult> = emptyList(),
    val notes: List<NoteEntry> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
