package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnitConverterActTest {

    @Test
    fun launch_defaultsToPressure_atmToKpa() {
        ActivityScenario.launch(UnitConverterAct::class.java).use {
            onView(withId(R.id.editConverterValue)).perform(replaceText("1"))
            onView(withId(R.id.tvConverterResult)).check(matches(withText("1.00 atm = 101.33 kPa")))
        }
    }

    @Test
    fun switchToMassCategory_updatesUnitsAndResult() {
        ActivityScenario.launch(UnitConverterAct::class.java).use {
            onView(withId(R.id.chipMass)).perform(click())
            onView(withId(R.id.editConverterValue)).perform(replaceText("2"))
            // Default from/to reset to index 0/1 on category switch: kg -> g
            onView(withId(R.id.tvConverterResult)).check(matches(withText("2.00 kg = 2000.00 g")))
        }
    }

    @Test
    fun switchToVolumeCategory_updatesUnitsAndResult() {
        ActivityScenario.launch(UnitConverterAct::class.java).use {
            onView(withId(R.id.chipVolume)).perform(click())
            onView(withId(R.id.editConverterValue)).perform(replaceText("1.5"))
            onView(withId(R.id.tvConverterResult)).check(matches(withText("1.50 L = 1500.00 mL")))
        }
    }

    @Test
    fun swapButton_flipsFromAndToUnits() {
        ActivityScenario.launch(UnitConverterAct::class.java).use {
            onView(withId(R.id.editConverterValue)).perform(replaceText("1"))
            onView(withId(R.id.btnConverterSwap)).perform(click())
            // After swap: from=kPa(index1), to=atm(index0) -> 1 kPa = 0.01 atm
            onView(withId(R.id.tvConverterResult)).check(matches(withText("1.00 kPa = 0.01 atm")))
        }
    }

    @Test
    fun negativeValue_showsErrorAndClearsResult() {
        ActivityScenario.launch(UnitConverterAct::class.java).use { scenario ->
            onView(withId(R.id.editConverterValue)).perform(replaceText("-5"))
            onView(withId(R.id.tvConverterResult)).check(matches(withText("")))
            scenario.onActivity { activity ->
                val layout = activity.findViewById<TextInputLayout>(R.id.layoutConverterInput)
                assertEquals(activity.getString(R.string.unit_converter_negative_error), layout.error?.toString())
            }
        }
    }

    @Test
    fun blankValue_clearsResult_withoutError() {
        ActivityScenario.launch(UnitConverterAct::class.java).use { scenario ->
            onView(withId(R.id.editConverterValue)).perform(replaceText(""))
            onView(withId(R.id.tvConverterResult)).check(matches(withText("")))
            scenario.onActivity { activity ->
                val layout = activity.findViewById<TextInputLayout>(R.id.layoutConverterInput)
                assertNull(layout.error)
            }
        }
    }

    @Test
    fun categorySwitch_afterSwap_resetsFromToDefaults() {
        ActivityScenario.launch(UnitConverterAct::class.java).use {
            onView(withId(R.id.btnConverterSwap)).perform(click())
            onView(withId(R.id.chipMass)).perform(click())
            onView(withId(R.id.editConverterValue)).perform(replaceText("1"))
            // Switching category must reset from/to to index 0/1, not keep the swapped indices
            onView(withId(R.id.tvConverterResult)).check(matches(withText("1.00 kg = 1000.00 g")))
        }
    }
}
