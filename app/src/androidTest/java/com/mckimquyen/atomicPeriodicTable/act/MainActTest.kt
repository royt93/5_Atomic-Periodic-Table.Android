package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActTest {

    @Test
    fun testMainActivityLaunch() {
        // Launch MainAct
        ActivityScenario.launch(MainAct::class.java).use {
            // Verify that main views are displayed
            onView(withId(R.id.scrollView)).check(matches(isDisplayed()))
            onView(withId(R.id.topBar)).check(matches(isDisplayed()))
            onView(withId(R.id.leftBar)).check(matches(isDisplayed()))
        }
    }

    // Regression guard for FIX-004: initBoiling/initMelting/initPhase/initYear/initElectro
    // used to call initName(elementList) where elementList was an always-empty field,
    // instead of initName(list) (the real, populated parameter). Running every hover-menu
    // lens over the full 118-element dataset exercises initName() with real data end to
    // end and must not throw (e.g. the findViewById/getIdentifier calls inside it).
    @Test
    fun allHoverMenuLenses_runOverFullElementList_withoutCrashing() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val elements = ArrayList<Element>()
                ElementModel.getList(elements)

                activity.initGroups(elements)
                activity.initBoiling(elements)
                activity.initMelting(elements)
                activity.initPhase(elements)
                activity.initYear(elements)
                activity.initElectro(elements)
                activity.initWeight(elements)
                activity.initHeat(elements)
                activity.initSpecific(elements)
                activity.initVape(elements)
            }
        }
    }
}
