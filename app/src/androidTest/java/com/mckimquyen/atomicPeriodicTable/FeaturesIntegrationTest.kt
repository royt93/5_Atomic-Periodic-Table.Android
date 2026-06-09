package com.mckimquyen.atomicPeriodicTable

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mckimquyen.atomicPeriodicTable.act.*
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.NotesPref
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeaturesIntegrationTest {

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.init(appContext)
    }

    @Test
    fun testElementNotes() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sendPref = ElementSendAndLoad(appContext)
        sendPref.setValue("hydrogen")

        // Clear any previous note
        val notesPref = NotesPref(appContext)
        notesPref.saveNote("hydrogen", "")

        ActivityScenario.launch(ElementInfoAct::class.java).use {
            // Check that input is initially empty
            onView(withId(R.id.notesInput)).perform(scrollTo()).check(matches(withText("")))

            // Type a note
            val noteText = "This is a test note for Hydrogen"
            onView(withId(R.id.notesInput)).perform(scrollTo(), replaceText(noteText))

            // Click save
            onView(withId(R.id.notesSaveBtn)).perform(scrollTo(), click())

            // Check that it's saved in preference
            assertEquals(noteText, notesPref.getNote("hydrogen"))
        }
    }

    @Test
    fun testChemicalEquationBalancer() {
        ActivityScenario.launch(EquationBalancerAct::class.java).use {
            // Type equation
            onView(withId(R.id.balancerInput)).perform(replaceText("H2 + O2 = H2O"))

            // Click balance
            onView(withId(R.id.balancerBtn)).perform(click())

            // Check result is displayed and correct
            onView(withId(R.id.balancerResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.balancerResultText)).check(matches(withText("2 H2 + O2 = 2 H2O")))
        }
    }

    @Test
    fun testMolarMassCalculator() {
        ActivityScenario.launch(CalculatorAct::class.java).use {
            // Type formula
            onView(withId(R.id.calcInput)).perform(replaceText("H2O"))

            // Click calculate
            onView(withId(R.id.calcBtn)).perform(click())

            // Check result is displayed and correct (contains "18.01")
            onView(withId(R.id.calcResultCard)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.calcResultText)).check(matches(withText(containsString("18.01"))))
        }
    }

    @Test
    fun testQuizActivity() {
        ActivityScenario.launch(QuizAct::class.java).use {
            // Check progress is displayed
            onView(withId(R.id.quizProgress)).check(matches(isDisplayed()))
            onView(withId(R.id.questionCard)).check(matches(isDisplayed()))

            // Click option 1
            onView(withId(R.id.option1Card)).perform(click())

            // Wait 2 seconds for progress to advance
            Thread.sleep(2000)

            // Verify the progress text or that the next question is shown (contains "2/10")
            onView(withId(R.id.quizProgress)).check(matches(withText(containsString("2/10"))))
        }
    }
}
