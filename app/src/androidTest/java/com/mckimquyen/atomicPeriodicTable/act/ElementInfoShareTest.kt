package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Regression guard for the "share element as image" feature: tapping the share button must
// render+save a real, non-empty PNG to cacheDir (the file FileProvider will expose) without
// crashing — ElementSendAndLoad.getValue() defaults to "hydrogen" so no seeding needed.
@RunWith(AndroidJUnit4::class)
class ElementInfoShareTest {

    @Test
    fun tapShareButton_writesNonEmptyPngToCacheDir() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val expectedFile = File(context.cacheDir, "element_H.png")
        expectedFile.delete()

        ActivityScenario.launch(ElementInfoAct::class.java).use {
            onView(withId(R.id.shareElementBtn)).perform(click())
            Thread.sleep(500) // bitmap render + file write + chooser Intent dispatch

            assertTrue("expected ${expectedFile.absolutePath} to exist", expectedFile.exists())
            assertTrue("expected non-empty PNG file", expectedFile.length() > 0)

            // The share click opens a system chooser Activity on top of ours — best-effort
            // dismiss so it doesn't linger into whichever test runs next. Not the test's actual
            // assertion (already passed above), so a flaky lifecycle state here shouldn't fail it.
            try {
                pressBackUnconditionally()
            } catch (_: Exception) {
            }
        }
    }
}
