package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// Regression guard for the TTS pronunciation feature (vòng 4 mục 13): the speak button must
// end up enabled once TTS init succeeds, and a tap must never crash the app. (An earlier version
// of this test also asserted the button starts disabled immediately after launch — dropped:
// ActivityScenario's onActivity{} only hands back control once the main-thread Looper is idle,
// and on a fast local TTS engine the init callback's posted Runnable can already have run by
// then, so that assertion was inherently racy rather than a real behavior guarantee.)
@RunWith(AndroidJUnit4::class)
class ElementInfoSpeakTest {

    private fun waitUntilSpeakButtonEnabled(scenario: ActivityScenario<ElementInfoAct>): Boolean {
        var enabled = false
        var attempts = 0
        while (!enabled && attempts < 20) {
            Thread.sleep(150)
            scenario.onActivity { activity ->
                enabled = activity.findViewById<android.view.View>(R.id.speakElementBtn).isEnabled
            }
            attempts++
        }
        return enabled
    }

    @Test
    fun speakButton_becomesEnabledOnceTtsInitializes() {
        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            val enabled = waitUntilSpeakButtonEnabled(scenario)
            assertTrue("expected TTS to become usable on a device/emulator with a TTS engine installed", enabled)
        }
    }

    @Test
    fun tappingSpeakButton_onceReady_doesNotCrash() {
        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            assertTrue("setup: TTS must be ready before testing tap", waitUntilSpeakButtonEnabled(scenario))

            onView(withId(R.id.speakElementBtn)).perform(click())
            Thread.sleep(300) // speak() dispatch
            // Reaching this line without the instrumentation process crashing is the pass condition.
        }
    }
}
