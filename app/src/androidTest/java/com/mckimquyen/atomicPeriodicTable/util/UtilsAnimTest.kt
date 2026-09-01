package com.mckimquyen.atomicPeriodicTable.util

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.MainAct
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for FIX-027: Utils.fadeOutAnim() used to schedule
 * view.postDelayed({ view.visibility = GONE }, time + 1) independently of the actual
 * animation. Calling fadeInAnim() right after fadeOutAnim() (before that delay elapsed) used
 * to leave the stale callback to force the view back to GONE afterwards, undoing the fade-in.
 */
@RunWith(AndroidJUnit4::class)
class UtilsAnimTest {

    @Test
    fun fadeOutThenFadeInImmediately_endsUpVisible_notForcedGoneByStaleCallback() {
        ActivityScenario.launch(MainAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val view = activity.findViewById<View>(R.id.scrollView)
                view.visibility = View.VISIBLE
                view.alpha = 1f

                Utils.fadeOutAnim(view, 200)
                Utils.fadeInAnim(view, 50)
            }

            // Wait past both animations' duration so any stale delayed callback would have
            // already fired if the bug were still present.
            Thread.sleep(500)

            scenario.onActivity { activity ->
                val view = activity.findViewById<View>(R.id.scrollView)
                assertEquals(
                    "fadeInAnim() right after fadeOutAnim() must not be undone by a stale GONE callback",
                    View.VISIBLE,
                    view.visibility,
                )
            }
        }
    }
}
