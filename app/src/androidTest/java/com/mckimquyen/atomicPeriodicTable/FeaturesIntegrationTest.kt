package com.mckimquyen.atomicPeriodicTable

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import com.mckimquyen.atomicPeriodicTable.act.*
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.NotesPref
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            val noteText = "This is a test note for Hydrogen"
            scenario.onActivity { act ->
                val input = act.findViewById<android.widget.EditText>(R.id.notesInput)
                val btn = act.findViewById<android.view.View>(R.id.notesSaveBtn)
                input.setText(noteText)
                btn.performClick()
            }

            // Check that it's saved in preference
            assertEquals(noteText, notesPref.getNote("hydrogen"))
        }
    }

    private fun nestedScrollTo(): androidx.test.espresso.ViewAction = object : androidx.test.espresso.ViewAction {
        override fun getConstraints(): org.hamcrest.Matcher<View> = org.hamcrest.Matchers.allOf(
            androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE),
            androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA(org.hamcrest.Matchers.anyOf(
                androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(androidx.core.widget.NestedScrollView::class.java),
                androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(android.widget.ScrollView::class.java)
            ))
        )

        override fun getDescription(): String = "Scroll to view inside NestedScrollView"

        override fun perform(uiController: androidx.test.espresso.UiController, view: View) {
            var parent = view.parent
            while (parent != null) {
                if (parent is androidx.core.widget.NestedScrollView) {
                    val rect = android.graphics.Rect()
                    view.getDrawingRect(rect)
                    (parent as android.view.ViewGroup).offsetDescendantRectToMyCoords(view, rect)
                    parent.scrollTo(0, rect.top)
                    uiController.loopMainThreadUntilIdle()
                    return
                }
                parent = parent.parent
            }
        }
    }

    @Test
    fun testElementNotes_LegacyMigration() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Seed a note under the legacy "note_" prefix (pre-migration storage format).
        val prefs = appContext.getSharedPreferences("Element_Notes_Preference", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("note_helium", "Legacy helium note")
            .remove("notes_helium")
            .apply()

        val notesPref = NotesPref(appContext)

        // Reading transparently migrates the value to the new "notes_" key.
        assertEquals("Legacy helium note", notesPref.getNote("helium"))
        assertEquals("Legacy helium note", prefs.getString("notes_helium", null))
        assertNull("Legacy key should be removed after migration", prefs.getString("note_helium", null))

        // Cleanup
        notesPref.saveNote("helium", "")
    }

    @Test
    fun testElementNotes_PersistAcrossRelaunch() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val sendPref = ElementSendAndLoad(appContext)
        sendPref.setValue("hydrogen")

        val notesPref = NotesPref(appContext)
        notesPref.saveNote("hydrogen", "Persisted note")

        // First launch: the saved note is loaded into the input.
        ActivityScenario.launch(ElementInfoAct::class.java).use {
            onView(withId(R.id.notesInput)).perform(scrollTo()).check(matches(withText("Persisted note")))
        }

        // Relaunch: the note survives because it lives in SharedPreferences.
        ActivityScenario.launch(ElementInfoAct::class.java).use {
            onView(withId(R.id.notesInput)).perform(scrollTo()).check(matches(withText("Persisted note")))
        }

        // Cleanup
        notesPref.saveNote("hydrogen", "")
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
            onView(withId(R.id.calcResultText)).check(matches(withText(anyOf(containsString("18.01"), containsString("18,01")))))
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
