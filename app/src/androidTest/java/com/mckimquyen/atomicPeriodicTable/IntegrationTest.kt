package com.mckimquyen.atomicPeriodicTable

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.act.MainAct
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    @Test
    fun testNavigateFromMainToSettings() {
        // Launch MainAct
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            // Verify main layout is displayed
            onView(withId(R.id.scrollView)).check(matches(isDisplayed()))

            // Explicitly make the navigation bar visible since it fades out on onCreate
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.navBarMain).visibility = View.VISIBLE
            }

            // Click settings button programmatically to avoid Espresso click synchronization/animation constraints
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.settingsBtn).performClick()
            }
            Thread.sleep(1000)

            // Verify settings screen is opened and scrollSettings is visible
            onView(withId(R.id.scrollSettings)).check(matches(isDisplayed()))
        }
    }
}
