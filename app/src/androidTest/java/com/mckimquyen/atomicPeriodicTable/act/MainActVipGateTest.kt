@file:OptIn(com.roy.sdkadbmob.InternalAdApi::class)

package com.mckimquyen.atomicPeriodicTable.act

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.vip.VipKeys
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.clearAppPreferencesForTest
import com.roy.sdkadbmob.resetVipActivationBackoffForTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Bug 7 fix: VIP pill (btnVipMenu / tvVipPillLabel) displays
 * the correct label depending on VIP state, and is always visible in MainAct.
 *
 * bindToolbarVipBadge() is called from onResume so the pill is updated every time
 * the activity comes to the foreground.
 */
@RunWith(AndroidJUnit4::class)
class MainActVipGateTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        // vipRedeemCodes marks each code used-per-device permanently; clearVipByKey() only
        // resets the expiry, not that mark — reset the whole store so VipKeys.VIP_30D_KEY can
        // be redeemed again by the next @Test. Also reset the shared brute-force backoff.
        AdManager.clearAppPreferencesForTest(ctx)
        AdManager.resetVipActivationBackoffForTest()
    }

    // ---- Free state ----

    @Test
    fun freeState_pillLabel_showsFreeText() {
        // Ensure VIP is not active
        AdManager.clearVipByKey()

        ActivityScenario.launch(MainAct::class.java).use {
            onView(withId(R.id.tvVipPillLabel))
                .check(matches(withText(R.string.vip_pill_free)))
        }
    }

    // ---- VIP active state ----

    @Test
    fun vipActive_pillLabel_showsVipTitle() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        ActivityScenario.launch(MainAct::class.java).use {
            onView(withId(R.id.tvVipPillLabel))
                .check(matches(withText(R.string.vip_title)))
        }
    }

    @Test
    fun vipActive_btnVipMenu_isDisplayed() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        ActivityScenario.launch(MainAct::class.java).use {
            onView(withId(R.id.btnVipMenu))
                .check(matches(isDisplayed()))
        }
    }
}
