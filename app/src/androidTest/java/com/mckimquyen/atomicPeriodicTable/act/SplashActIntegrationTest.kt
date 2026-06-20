package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Bug 11 fix: SplashAct now extends BaseAct.
 *
 * These tests confirm that SplashAct launches without crashing (previously it could
 * fail if the attachBaseContext chain was broken) and that its core views are visible.
 */
@RunWith(AndroidJUnit4::class)
class SplashActIntegrationTest {

    @Test
    fun splashActLaunches_withoutCrash() {
        ActivityScenario.launch(SplashAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity must not be null", activity)
                assertFalse("Activity must not be finishing immediately", activity.isFinishing)
            }
        }
    }

    @Test
    fun splashActHasCorrectViewsVisible() {
        ActivityScenario.launch(SplashAct::class.java).use {
            onView(withId(R.id.versionText)).check(matches(isDisplayed()))
        }
    }
}
