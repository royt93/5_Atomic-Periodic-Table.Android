package com.mckimquyen.atomicPeriodicTable.act

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.hamcrest.CoreMatchers.containsString
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
}
