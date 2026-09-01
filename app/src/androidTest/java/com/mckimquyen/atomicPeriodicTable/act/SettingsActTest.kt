package com.mckimquyen.atomicPeriodicTable.act

import android.widget.ScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Regression guard for FIX-029: the "clear cache" action used to call
 * cacheDir.deleteRecursively(), which deletes the cache directory itself (not just its
 * contents) — a directory Android guarantees exists for the app's lifetime. Clearing cache
 * must empty it, not remove it.
 */
@RunWith(AndroidJUnit4::class)
class SettingsActTest {

    @Test
    fun clearCache_emptiesCacheDir_withoutDeletingTheDirectoryItself() {
        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            var cacheDir: File? = null
            var dummyFile: File? = null

            scenario.onActivity { activity ->
                cacheDir = activity.cacheDir
                dummyFile = File(activity.cacheDir, "fix029_test_file.tmp").apply { writeText("x") }
                // cacheLay sits below the fold in scrollSettings — scroll to it programmatically
                // (same FIX-038 reasoning: a synthesized swipe gesture risks hitting the OS
                // gesture-navigation zone near the screen edge on real devices).
                val scrollView = activity.findViewById<ScrollView>(R.id.scrollSettings)
                val row = activity.findViewById<android.view.View>(R.id.clearCacheSettings)
                val rowLoc = IntArray(2).also { row.getLocationInWindow(it) }
                val scrollViewLoc = IntArray(2).also { scrollView.getLocationInWindow(it) }
                scrollView.scrollTo(0, scrollView.scrollY + (rowLoc[1] - scrollViewLoc[1]))
            }
            assertTrue("setup: dummy file must exist before clearing cache", dummyFile!!.exists())

            onView(withId(R.id.clearCacheSettings)).perform(click())

            assertTrue("cacheDir itself must still exist after clearing cache", cacheDir!!.exists())
            assertTrue("cacheDir must still be a directory after clearing cache", cacheDir!!.isDirectory)
            assertFalse("cache contents must be gone after clearing cache", dummyFile!!.exists())
        }
    }
}
