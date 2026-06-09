package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
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
}
