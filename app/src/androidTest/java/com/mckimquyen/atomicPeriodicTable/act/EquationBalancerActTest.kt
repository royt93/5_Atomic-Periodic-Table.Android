package com.mckimquyen.atomicPeriodicTable.act

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EquationBalancerActTest {

    @Test
    fun testBalancerInitialState() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerTitleText)).check(matches(isDisplayed()))
            onView(withId(R.id.balancerInput)).check(matches(isDisplayed()))
            onView(withId(R.id.balancerBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.balancerResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testBalancerValidation_EmptyInput() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerBtn)).perform(click())
            onView(withId(R.id.balancerResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testBalancerValidation_InvalidFormat() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("H2 + O2"))
            onView(withId(R.id.balancerBtn)).perform(click())

            // Result card should not show
            onView(withId(R.id.balancerResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testBalancerBackPress() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.backBtn).performClick()
                assertTrue(activity.isFinishing)
            }
        }
    }
}
