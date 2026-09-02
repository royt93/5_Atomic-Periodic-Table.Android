package com.mckimquyen.atomicPeriodicTable.act

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.history.RecentlyViewedPref
import com.mckimquyen.atomicPeriodicTable.feature.streak.StudyStreakPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActTest {

    @Test
    fun noStudySessionYet_streakIndicatorIsHidden() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val tv = activity.findViewById<android.view.View>(R.id.tvStudyStreak)
                assertSame(android.view.View.GONE, tv.visibility)
            }
        }
    }

    @Test
    fun existingStreak_showsIndicatorWithDayCount() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Study_Streak_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        StudyStreakPref(context).recordStudyToday(todayEpochDay = 20000L)

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val tv = activity.findViewById<android.widget.TextView>(R.id.tvStudyStreak)
                assertSame(android.view.View.VISIBLE, tv.visibility)
                assertNotNull(tv.text)
                assert(tv.text.contains("1")) { "expected streak text to mention 1 day, got ${tv.text}" }
            }
        }
    }

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

    // Regression guard for the Recently Viewed feature (vòng 4 mục 14): row must stay hidden
    // when there's no history — no wasted header space on first launch.
    @Test
    fun noRecentlyViewedHistory_rowIsHidden() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Recently_Viewed_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val row = activity.findViewById<android.view.View>(R.id.recentlyViewedRow)
                assertSame(android.view.View.GONE, row.visibility)
            }
        }
    }

    @Test
    fun existingRecentlyViewedHistory_rowShowsOneChipPerSymbol() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Recently_Viewed_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        RecentlyViewedPref(context).recordViewed("H")
        RecentlyViewedPref(context).recordViewed("He")

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val row = activity.findViewById<android.view.View>(R.id.recentlyViewedRow)
                assertSame(android.view.View.VISIBLE, row.visibility)

                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupRecentlyViewed)
                assertEquals(2, chipGroup.childCount)
            }
        }
    }

    @Test
    fun clickingElementOnGrid_recordsRecentlyViewed() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Recently_Viewed_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val resId = activity.resources.getIdentifier("hydrogen_btn", "id", activity.packageName)
                activity.findViewById<android.view.View>(resId).performClick()
            }
            Thread.sleep(300)

            assertTrue(RecentlyViewedPref(context).getRecent().contains("H"))

            // Click navigates to ElementInfoAct (or via interstitial) outside this scenario's
            // ownership — close it best-effort so it doesn't linger and steal focus from the
            // next test's fresh MainAct launch.
            try {
                pressBackUnconditionally()
            } catch (_: Exception) {
            }
        }
    }

    @Test
    fun tappingRecentlyViewedChip_setsElementSendAndLoadValueForThatElement() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("Recently_Viewed_Preference", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        RecentlyViewedPref(context).recordViewed("He")

        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val expectedName = elements.first { it.short == "He" }.element

        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupRecentlyViewed)
                (chipGroup.getChildAt(0) as Chip).performClick()
            }
            Thread.sleep(300)

            assertEquals(expectedName, ElementSendAndLoad(context).getValue())

            try {
                pressBackUnconditionally()
            } catch (_: Exception) {
            }
        }
    }

    // Advanced Element Filter (vòng 5 mục 19): dims (alpha 0.25f) grid cells that don't match a
    // mass/electronegativity/category filter, without ever hiding them (GONE) — layout positions
    // must never shift while a filter is active.
    private fun alphaOf(activity: android.app.Activity, elementName: String): Float {
        val resId = activity.resources.getIdentifier("${elementName}_btn", "id", activity.packageName)
        return activity.findViewById<android.view.View>(resId).alpha
    }

    @Test
    fun applyingMassFilter_dimsNonMatchingElements_keepsMatchingAtFullAlpha() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.filterElementsBtn).performClick()
            }
            Thread.sleep(300)

            onView(withId(R.id.filterMinMass)).inRoot(isDialog()).perform(replaceText("3"))
            onView(withId(R.id.filterMaxMass)).inRoot(isDialog()).perform(replaceText("5"))
            onView(withText(R.string.filter_apply)).inRoot(isDialog()).perform(click())
            Thread.sleep(300)

            scenario.onActivity { activity ->
                assertEquals(0.25f, alphaOf(activity, "hydrogen"), 0.001f)
                assertEquals(1.0f, alphaOf(activity, "helium"), 0.001f)
                assertEquals(0.25f, alphaOf(activity, "lithium"), 0.001f)
            }
        }
    }

    @Test
    fun clearingFilter_resetsAllElementsToFullAlpha() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.filterElementsBtn).performClick()
            }
            Thread.sleep(300)
            onView(withId(R.id.filterMinMass)).inRoot(isDialog()).perform(replaceText("3"))
            onView(withId(R.id.filterMaxMass)).inRoot(isDialog()).perform(replaceText("5"))
            onView(withText(R.string.filter_apply)).inRoot(isDialog()).perform(click())
            Thread.sleep(300)
            scenario.onActivity { activity ->
                assertEquals(0.25f, alphaOf(activity, "hydrogen"), 0.001f) // sanity: filter is active
            }

            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.filterElementsBtn).performClick()
            }
            Thread.sleep(300)
            onView(withText(R.string.filter_clear)).inRoot(isDialog()).perform(click())
            Thread.sleep(300)

            scenario.onActivity { activity ->
                assertEquals(1.0f, alphaOf(activity, "hydrogen"), 0.001f)
                assertEquals(1.0f, alphaOf(activity, "helium"), 0.001f)
                assertEquals(1.0f, alphaOf(activity, "lithium"), 0.001f)
            }
        }
    }

    @Test
    fun applyingCategoryFilter_matchesOnlyThatCategory() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            var nobleGasesLabel = ""
            scenario.onActivity { activity ->
                // Computed from the Activity's own (LocaleHelper-wrapped) context — see mục 15's
                // launchingWithCategoryFilter test for why ApplicationProvider's raw context
                // isn't equivalent (app language default vs. device system locale).
                nobleGasesLabel = com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator.translate(activity, "Noble Gases")
                activity.findViewById<android.view.View>(R.id.filterElementsBtn).performClick()
            }
            Thread.sleep(300)

            // Material Chip's built-in state/ripple animator never reports "idle" to Espresso's
            // GeneralClickAction (it waits for animations to settle after injecting the tap),
            // which surfaces as a PerformException ("Animations or transitions are enabled") on
            // real devices with animator scales on — this is unrelated to app/test correctness.
            // performClick() inside a ViewAssertion callback (guaranteed to run on the UI thread)
            // clicks the Chip directly, bypassing that wait entirely.
            onView(withText(nobleGasesLabel)).inRoot(isDialog()).check { view, _ -> view.performClick() }
            onView(withText(R.string.filter_apply)).inRoot(isDialog()).perform(click())
            Thread.sleep(300)

            scenario.onActivity { activity ->
                assertEquals(0.25f, alphaOf(activity, "hydrogen"), 0.001f)
                assertEquals(1.0f, alphaOf(activity, "helium"), 0.001f) // Noble Gases
                assertEquals(0.25f, alphaOf(activity, "lithium"), 0.001f)
            }
        }
    }
}
