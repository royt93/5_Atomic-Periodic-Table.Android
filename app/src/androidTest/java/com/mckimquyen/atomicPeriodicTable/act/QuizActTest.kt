package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.exam.ExamHistoryPref
import com.mckimquyen.atomicPeriodicTable.feature.quiz.QuizBestScorePref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizActTest {

    @Test
    fun testQuizInitialState() {
        ActivityScenario.launch(QuizAct::class.java).use {
            // Check that the title, progress, score, and options are visible
            onView(withId(R.id.quizTitleText)).check(matches(isDisplayed()))
            onView(withId(R.id.quizProgress)).check(matches(isDisplayed()))
            onView(withId(R.id.quizScore)).check(matches(isDisplayed()))
            onView(withId(R.id.questionText)).check(matches(isDisplayed()))
            onView(withId(R.id.optionsContainer)).check(matches(isDisplayed()))

            onView(withId(R.id.option1Card)).check(matches(isDisplayed()))
            onView(withId(R.id.option2Card)).check(matches(isDisplayed()))
            onView(withId(R.id.option3Card)).check(matches(isDisplayed()))
            onView(withId(R.id.option4Card)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testQuizBackPress() {
        ActivityScenario.launch(QuizAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.backBtn).performClick()
                assertTrue(activity.isFinishing)
            }
        }
    }

    @Test
    fun testQuizScoreTimerAndProgressDisplayed() {
        ActivityScenario.launch(QuizAct::class.java).use {
            onView(withId(R.id.quizScore)).check(matches(isDisplayed()))
            onView(withId(R.id.quizProgressBar)).check(matches(isDisplayed()))
            onView(withId(R.id.timerContainer)).check(matches(isDisplayed()))
            onView(withId(R.id.timerText)).check(matches(isDisplayed()))

            // The quiz is a fixed 10-question run.
            onView(withId(R.id.quizProgress)).check(matches(withText(containsString("/10"))))
        }
    }

    @Test
    fun testQuizStartsAtFirstQuestion() {
        ActivityScenario.launch(QuizAct::class.java).use {
            onView(withId(R.id.quizProgress)).check(matches(withText(containsString("1/10"))))
        }
    }

    @Test
    fun testQuizSelectionAdvancesToNextQuestion() {
        ActivityScenario.launch(QuizAct::class.java).use {
            // Pick any option; after the 1.5s auto-advance the quiz moves on.
            onView(withId(R.id.option1Card)).perform(click())
            Thread.sleep(2000)
            onView(withId(R.id.quizProgress)).check(matches(withText(containsString("2/10"))))
        }
    }

    // Regression guard for FIX-006: checkAnswer()/handleTimesUp() schedule a 1500ms
    // postDelayed(currentQuestionIndex++; generateQuestion()) that used to keep running
    // after the Activity was destroyed. This test destroys the Activity immediately after
    // answering and waits past the 1500ms window — before the fix this could touch
    // `binding`/animators of a dead Activity from the main Looper; after the fix the
    // callback is removed in onDestroy() and never fires. There is no direct way to
    // assert "the callback did not run" from outside the Activity, so this is a
    // no-crash / no-leaked-callback smoke test rather than a precise state assertion.
    @Test
    fun answerQuestion_thenDestroyImmediately_noDelayedCallbackCrash() {
        val scenario = ActivityScenario.launch(QuizAct::class.java)
        onView(withId(R.id.option1Card)).perform(click())
        scenario.close()
        Thread.sleep(2000)
        // Reaching this line without the instrumentation process crashing/ANRing
        // is the pass condition.
    }

    // Regression guard for the study-streak feature: finishing a quiz (showResults()) must
    // record today's study streak. Clicking through all 10 real questions just to reach
    // showResults() would be slow and flaky (see FlashcardAct's 118-card test for that cost
    // at a much smaller scale) — showResults() is private, so invoke it via reflection instead,
    // same "call the private member directly" approach already used elsewhere in this suite
    // (e.g. IonActTest's filterHandler field check).
    @Test
    fun finishingQuiz_recordsStudyStreak() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        ActivityScenario.launch(QuizAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val method = QuizAct::class.java.getDeclaredMethod("showResults")
                method.isAccessible = true
                method.invoke(activity)
            }
            Thread.sleep(400) // fade-out/fade-in animation inside showResults()

            val streak = StudyStreakPref(context).getCurrentStreak()
            assertTrue("expected streak >= 1, got $streak", streak >= 1)
        }
    }

    // Practice Exam Mode (mục 8): QuizAct is parameterized via intent extras rather than
    // forked into a separate Activity (decision made 2026-09-02 to reuse the ~700 lines of
    // already-tested animation/question logic instead of duplicating it).
    @Test
    fun launchingInPracticeMode_showsCustomTitle_hidesTimer_usesExtraQuestionCount() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = android.content.Intent(context, QuizAct::class.java)
            .putExtra(QuizAct.EXTRA_PRACTICE_MODE, true)
            .putExtra(QuizAct.EXTRA_TOTAL_QUESTIONS, 20)

        ActivityScenario.launch<QuizAct>(intent).use {
            onView(withId(R.id.quizTitleText)).check(matches(withText(R.string.practice_exam_title)))
            onView(withId(R.id.timerContainer)).check(matches(not(isDisplayed())))
            onView(withId(R.id.quizProgress)).check(matches(withText(containsString("/20"))))
        }
    }

    @Test
    fun finishingPracticeExam_savesExamHistory_andShowsHistorySection() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Exam_History_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        val intent = android.content.Intent(context, QuizAct::class.java)
            .putExtra(QuizAct.EXTRA_PRACTICE_MODE, true)
            .putExtra(QuizAct.EXTRA_TOTAL_QUESTIONS, 5)

        ActivityScenario.launch<QuizAct>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val method = QuizAct::class.java.getDeclaredMethod("showResults")
                method.isAccessible = true
                method.invoke(activity)
            }
            Thread.sleep(1000) // fade-out (300ms) + result card entrance (400ms) + buffer

            val history = ExamHistoryPref(context).getHistory()
            assertTrue("expected at least 1 history entry, got ${history.size}", history.isNotEmpty())
            assertEquals(5, history[0].total)

            onView(withId(R.id.examHistoryText)).check(matches(isDisplayed()))
        }
    }

    // Exercises setupQuestionData() directly (bypassing the entrance/carousel animation chain)
    // across many random draws so the new molar-mass branch (questionType == 6, practice-only)
    // gets real coverage without the cost of clicking through dozens of real questions.
    @Test
    fun practiceMode_repeatedQuestionGeneration_neverCrashes_andAlwaysProducesFourDistinctChoices() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = android.content.Intent(context, QuizAct::class.java)
            .putExtra(QuizAct.EXTRA_PRACTICE_MODE, true)
            .putExtra(QuizAct.EXTRA_TOTAL_QUESTIONS, 10)

        ActivityScenario.launch<QuizAct>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val setupMethod = QuizAct::class.java.getDeclaredMethod("setupQuestionData")
                setupMethod.isAccessible = true
                val choicesField = QuizAct::class.java.getDeclaredField("currentChoices")
                choicesField.isAccessible = true

                repeat(200) {
                    setupMethod.invoke(activity)
                    @Suppress("UNCHECKED_CAST")
                    val choices = choicesField.get(activity) as ArrayList<String>
                    assertEquals(4, choices.size)
                    assertEquals("choices must be distinct: $choices", 4, choices.toSet().size)
                }
            }
        }
    }

    // Regression guard for the badges feature (mục 9): finishing a STANDARD (non-practice) quiz
    // must record the score into QuizBestScorePref so the "Perfect Quiz" badge can unlock —
    // this pref didn't exist before badges, standard Quiz previously discarded score on exit.
    @Test
    fun finishingStandardQuiz_recordsBestScore() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Quiz_Best_Score_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        ActivityScenario.launch(QuizAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val scoreField = QuizAct::class.java.getDeclaredField("score")
                scoreField.isAccessible = true
                scoreField.setInt(activity, 8)

                val method = QuizAct::class.java.getDeclaredMethod("showResults")
                method.isAccessible = true
                method.invoke(activity)
            }
            Thread.sleep(400) // fade-out/fade-in animation inside showResults()

            assertEquals(8, QuizBestScorePref(context).getBestScore())
        }
    }
}
