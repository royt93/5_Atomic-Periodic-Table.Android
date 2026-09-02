package com.mckimquyen.atomicPeriodicTable.feature.backup

import android.content.Context
import com.google.gson.Gson
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.NotesPref

/**
 * Aggregates progress from 4 existing prefs (Flashcard, Streak, Exam history, Notes) into one
 * JSON file via Gson. VIP is deliberately excluded — VipPrefs only holds display metadata, the
 * real entitlement lives server/SDK-side (see CLAUDE.md), so "restoring" it would mislead the
 * user into thinking VIP came back when it didn't. Import overwrites existing data outright
 * (no merge) — simpler and less error-prone than reconciling two histories.
 */
object BackupManager {
    private val gson = Gson()

    private fun allSymbols(): List<String> {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        return elements.map { it.short }
    }

    fun export(context: Context): String {
        val symbols = allSymbols()
        val flashcardPref = FlashcardPref(context)
        val notesPref = NotesPref(context)
        val streakPref = StudyStreakPref(context)

        val flashcards = symbols.map { symbol ->
            FlashcardEntry(symbol, flashcardPref.getState(symbol), flashcardPref.getNextReviewAtMs(symbol))
        }
        val notes = symbols.mapNotNull { symbol ->
            val note = notesPref.getNote(symbol)
            if (note.isNotEmpty()) NoteEntry(symbol, note) else null
        }

        val data = BackupData(
            flashcards = flashcards,
            currentStreak = streakPref.getCurrentStreak(),
            lastStreakEpochDay = streakPref.getLastEpochDay(),
            examHistory = ExamHistoryPref(context).getHistory(),
            notes = notes,
        )
        return gson.toJson(data)
    }

    fun import(context: Context, json: String): Result<Unit> {
        return try {
            val data = gson.fromJson(json, BackupData::class.java)
                ?: return Result.failure(IllegalArgumentException("Empty or malformed backup file"))
            if (data.schemaVersion > BackupData.CURRENT_SCHEMA_VERSION) {
                return Result.failure(IllegalArgumentException("Backup was made with a newer app version"))
            }

            val flashcardPref = FlashcardPref(context)
            for (entry in data.flashcards) {
                flashcardPref.saveState(entry.symbol, entry.state, entry.nextReviewAtMs)
            }

            val notesPref = NotesPref(context)
            for (entry in data.notes) {
                notesPref.saveNote(entry.symbol, entry.note)
            }

            StudyStreakPref(context).restore(data.currentStreak, data.lastStreakEpochDay)
            ExamHistoryPref(context).replaceHistory(data.examHistory)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
