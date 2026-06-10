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

    @Test
    fun testBalancer_ValidEquation() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("H2 + O2 = H2O"))
            onView(withId(R.id.balancerBtn)).perform(click())

            // Check if result is correct
            onView(withId(R.id.balancerResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.balancerResultText)).check(matches(withText("2 H2 + O2 = 2 H2O")))
        }
    }

    @Test
    fun testBalancer_LowercaseEquation() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("h2 + o2 = h2o"))
            onView(withId(R.id.balancerBtn)).perform(click())

            // Check if result is correctly capitalized and balanced
            onView(withId(R.id.balancerResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.balancerResultText)).check(matches(withText("2 H2 + O2 = 2 H2O")))
        }
    }

    @Test
    fun testBalancer_ArrowNotation() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("Fe + Cl2 -> FeCl3"))
            onView(withId(R.id.balancerBtn)).perform(click())

            onView(withId(R.id.balancerResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.balancerResultText)).check(matches(withText("2 Fe + 3 Cl2 = 2 FeCl3")))
        }
    }

    @Test
    fun testBalancer_ComplexCombustion() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("C3H8 + O2 = CO2 + H2O"))
            onView(withId(R.id.balancerBtn)).perform(click())

            onView(withId(R.id.balancerResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.balancerResultText)).check(matches(withText("C3H8 + 5 O2 = 3 CO2 + 4 H2O")))
        }
    }

    @Test
    fun testBalancer_UnbalanceableNoResult() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            onView(withId(R.id.balancerInput)).perform(replaceText("H2O = CO2"))
            onView(withId(R.id.balancerBtn)).perform(click())

            // Element mismatch cannot be balanced -> result stays hidden
            onView(withId(R.id.balancerResultCard)).check(matches(not(isDisplayed())))
        }
    }
}
