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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorActTest {

    @Before
    fun setUp() {
        val appContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.init(appContext)
    }

    @Test
    fun testCalculatorInitialState() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            onView(withId(R.id.elementTitle)).check(matches(isDisplayed()))
            onView(withId(R.id.calcInput)).check(matches(isDisplayed()))
            onView(withId(R.id.calcBtn)).check(matches(isDisplayed()))
            onView(withId(R.id.calcResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testCalculatorValidation_EmptyInput() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            onView(withId(R.id.calcBtn)).perform(click())
            onView(withId(R.id.calcResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testCalculatorValidation_InvalidFormula() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            onView(withId(R.id.calcInput)).perform(replaceText("XYZ"))
            onView(withId(R.id.calcBtn)).perform(click())

            // Result card should not show
            onView(withId(R.id.calcResultCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testCalculatorBackPress() {
        ActivityScenario.launch(CalculatorAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.backBtn).performClick()
                assertTrue(activity.isFinishing)
            }
        }
    }

    @Test
    fun testElementWeightCacheInitialized() {
        val appContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.init(appContext)
        val mass = com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.getMass("H")
        org.junit.Assert.assertNotNull("Hydrogen mass should not be null", mass)
        assertTrue("Hydrogen mass should be greater than zero", mass!! > 0.0)
    }

    @Test
    fun testCalculator_ValidFormula() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            onView(withId(R.id.calcInput)).perform(replaceText("H2O"))
            onView(withId(R.id.calcBtn)).perform(click())

            // Check if result card is visible
            onView(withId(R.id.calcResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.calcResultText)).check(matches(withText(org.hamcrest.CoreMatchers.containsString("18.01"))))
        }
    }

    @Test
    fun testCalculator_LowercaseFormula() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            onView(withId(R.id.calcInput)).perform(replaceText("h2o"))
            onView(withId(R.id.calcBtn)).perform(click())

            // Check if result card is visible and correctly parsed as H2O (18.015 g/mol)
            onView(withId(R.id.calcResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.calcResultText)).check(matches(withText(org.hamcrest.CoreMatchers.containsString("18.01"))))
        }
    }
}
