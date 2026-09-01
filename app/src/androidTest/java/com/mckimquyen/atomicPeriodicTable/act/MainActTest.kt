package com.mckimquyen.atomicPeriodicTable.act

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
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

    // Regression guard for FIX-008: searching used to discard the RecyclerView's attached
    // adapter and replace it with a brand-new ElementAdt instance on every keystroke
    // (losing scroll position); the "real" adapter field (mAdapter) was never attached to
    // the RecyclerView in the first place. After the fix, the same adapter instance stays
    // attached and filter() only calls mAdapter.filterList() on it.
    @Test
    fun searching_keepsSameAdapterInstance_insteadOfReplacingIt() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            onView(withId(R.id.searchBox)).perform(click())

            var adapterBefore: RecyclerView.Adapter<*>? = null
            scenario.onActivity { activity ->
                adapterBefore = activity.findViewById<RecyclerView>(R.id.rvElement).adapter
            }
            assertNotNull("adapter must already be attached before typing anything", adapterBefore)

            onView(withId(R.id.editElement)).perform(replaceText("hydro"))

            scenario.onActivity { activity ->
                val adapterAfter = activity.findViewById<RecyclerView>(R.id.rvElement).adapter
                assertSame(
                    "filtering must reuse the same adapter instance, not replace it",
                    adapterBefore,
                    adapterAfter,
                )
            }
        }
    }
}
