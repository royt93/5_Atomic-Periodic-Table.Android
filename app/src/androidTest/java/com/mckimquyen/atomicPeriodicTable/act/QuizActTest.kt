package com.mckimquyen.atomicPeriodicTable.act

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
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
}
