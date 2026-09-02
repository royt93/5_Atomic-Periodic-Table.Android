package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.quiz.QuizBestScorePref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BadgeActTest {

    @Before
    fun clearAllProgressPrefs() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        for (prefName in listOf("Study_Streak_Preference", "Quiz_Best_Score_Preference", "Exam_History_Preference", "Flashcard_Preference")) {
            context.getSharedPreferences(prefName, android.content.Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    // The 7 badge rows animate in with a staggered entrance (index * 60ms delay + 350ms each,
    // see BadgeAct.animateEntrance()) — wait past the last row's animation before reading alpha.
    private val entranceAnimationSettleMs = 900L

    @Test
    fun noProgress_allBadgesRenderAsLocked() {
        ActivityScenario.launch(BadgeAct::class.java).use { scenario ->
            Thread.sleep(entranceAnimationSettleMs)
            scenario.onActivity { activity ->
                val card = activity.findViewById<androidx.cardview.widget.CardView>(R.id.cardStreak3)
                assertTrue("locked badge must be dimmed", card.alpha < 1f)
            }
        }
    }

    @Test
    fun streakOfSeven_rendersStreak7CardAsUnlocked() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val streakPref = StudyStreakPref(context)
        // Fast-forward the streak to 7 by walking consecutive days.
        for (day in 20000L..20006L) {
            streakPref.recordStudyToday(todayEpochDay = day)
        }

        ActivityScenario.launch(BadgeAct::class.java).use { scenario ->
            Thread.sleep(entranceAnimationSettleMs)
            scenario.onActivity { activity ->
                val card = activity.findViewById<androidx.cardview.widget.CardView>(R.id.cardStreak7)
                assertTrue("streak-7 badge must be fully opaque once unlocked", card.alpha == 1f)
            }
        }
    }

    @Test
    fun perfectExamHistory_rendersPerfectExamCardAsUnlocked() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        ExamHistoryPref(context).addResult(score = 20, total = 20)

        ActivityScenario.launch(BadgeAct::class.java).use { scenario ->
            Thread.sleep(entranceAnimationSettleMs)
            scenario.onActivity { activity ->
                val card = activity.findViewById<androidx.cardview.widget.CardView>(R.id.cardPerfectExam)
                assertTrue(card.alpha == 1f)
            }
        }
    }

    @Test
    fun perfectQuizBestScore_rendersPerfectQuizCardAsUnlocked() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        QuizBestScorePref(context).recordScore(QuizAct.DEFAULT_QUESTION_COUNT)

        ActivityScenario.launch(BadgeAct::class.java).use { scenario ->
            Thread.sleep(entranceAnimationSettleMs)
            scenario.onActivity { activity ->
                val card = activity.findViewById<androidx.cardview.widget.CardView>(R.id.cardPerfectQuiz)
                assertTrue(card.alpha == 1f)
            }
        }
    }

    @Test
    fun backButton_finishesActivity() {
        ActivityScenario.launch(BadgeAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.badgeBackBtn).performClick()
                assertTrue(activity.isFinishing)
            }
        }
    }
}
