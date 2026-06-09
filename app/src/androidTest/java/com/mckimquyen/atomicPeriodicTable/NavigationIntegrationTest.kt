package com.mckimquyen.atomicPeriodicTable

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.act.MainAct
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @Test
    fun testNavigateFromMainToQuiz() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer
            onView(withId(R.id.menuBtn)).perform(click())
            Thread.sleep(1000)

            // Click Quiz menu button
            onView(withId(R.id.menuQuizBtn)).perform(click())
            Thread.sleep(1000)

            // Verify Quiz screen is visible
            onView(withId(R.id.quizProgress)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToBalancer() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer
            onView(withId(R.id.menuBtn)).perform(click())
            Thread.sleep(1000)

            // Click Balancer menu button
            onView(withId(R.id.menuBalancerBtn)).perform(click())
            Thread.sleep(1000)

            // Verify Balancer screen is visible
            onView(withId(R.id.balancerInput)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigateFromMainToCalculator() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Open navigation drawer
            onView(withId(R.id.menuBtn)).perform(click())
            Thread.sleep(1000)

            // Click Calculator menu button
            onView(withId(R.id.menuMolarMassBtn)).perform(click())
            Thread.sleep(1000)

            // Verify Calculator screen is visible
            onView(withId(R.id.calcInput)).check(matches(isDisplayed()))
        }
    }
}
