package com.mckimquyen.atomicPeriodicTable.feature.reset

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardPref
import com.mckimquyen.atomicPeriodicTable.feature.flashcard.FlashcardState
import com.mckimquyen.atomicPeriodicTable.feature.quiz.QuizBestScorePref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.feature.vip.VipPrefs
import com.mckimquyen.atomicPeriodicTable.pref.NotesPref
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressResetManagerTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun clearAllRelevantPrefs() {
        for (name in listOf(
            "Study_Streak_Preference",
            "Flashcard_Preference",
            "Exam_History_Preference",
            "Quiz_Best_Score_Preference",
            "vip_screen_prefs",
            "Element_Notes_Preference",
        )) {
            context.getSharedPreferences(name, android.content.Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test
    fun resetAll_wipesStreakFlashcardExamHistoryAndBestScore() {
        StudyStreakPref(context).recordStudyToday(todayEpochDay = 20000L)
        FlashcardPref(context).saveState("H", FlashcardState(easeFactor = 2.8f, intervalDays = 6, repetitions = 3), nextReviewAtMs = 999L)
        ExamHistoryPref(context).addResult(score = 8, total = 10)
        QuizBestScorePref(context).recordScore(9)

        ProgressResetManager.resetAll(context)

        assertEquals(0, StudyStreakPref(context).getCurrentStreak())
        assertEquals(0L, StudyStreakPref(context).getLastEpochDay())
        assertEquals(0, FlashcardPref(context).getState("H").repetitions)
        assertEquals(0L, FlashcardPref(context).getNextReviewAtMs("H"))
        assertTrue(ExamHistoryPref(context).getHistory().isEmpty())
        assertEquals(0, QuizBestScorePref(context).getBestScore())
    }

    @Test
    fun resetAll_doesNotTouchNotesOrVipEntitlement() {
        NotesPref(context).saveNote("H", "my study note")
        VipPrefs(context).saveGrantedAtMs(123456L)
        VipPrefs(context).saveActivatedDays(30)
        VipPrefs(context).markUserRedeemed()

        ProgressResetManager.resetAll(context)

        assertEquals("my study note", NotesPref(context).getNote("H"))
        assertEquals(123456L, VipPrefs(context).getGrantedAtMs())
        assertEquals(30, VipPrefs(context).getActivatedDays())
        assertTrue(VipPrefs(context).userRedeemedAtLeastOnce())
    }

    @Test
    fun resetAll_onAlreadyEmptyState_doesNotCrash() {
        // No setup — everything already at defaults from clearAllRelevantPrefs().
        ProgressResetManager.resetAll(context)

        assertEquals(0, StudyStreakPref(context).getCurrentStreak())
        assertTrue(ExamHistoryPref(context).getHistory().isEmpty())
        assertEquals(0, QuizBestScorePref(context).getBestScore())
    }
}
