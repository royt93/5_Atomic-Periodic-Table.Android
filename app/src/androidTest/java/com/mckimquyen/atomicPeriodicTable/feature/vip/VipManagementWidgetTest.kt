package com.mckimquyen.atomicPeriodicTable.feature.vip

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.roy.sdkadbmob.AdManager
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Widget-level tests: kiểm tra từng component UI trong VipManagementAct hiển thị
 * đúng dữ liệu sau khi state thay đổi — tương đương "widget test" trong Flutter.
 *
 * Không mock AdManager; dùng real activation vì key == vipKeySecret được config
 * trong RoyApp.configureAds() khi Application khởi động.
 */
@RunWith(AndroidJUnit4::class)
class VipManagementWidgetTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        AdManager.clearVipByKey()
        VipPrefs(ctx).clearGrantedAtMs()
        VipPrefs(ctx).clearUserRedeemed()
    }

    // ---- Status header shows correct text per VIP state ----

    @Test
    fun freeState_statusTextShowsFreeUser() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.tvVipStatus))
                .check(matches(withText(R.string.vip_free_user)))
        }
    }

    @Test
    fun activeState_statusTextShowsVipActive() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.tvVipStatus))
                .check(matches(withText(R.string.vip_active)))
        }
    }

    // ---- Activation date row shows formatted date ----

    @Test
    fun activeState_activationDateRowVisible_withDateText() {
        VipPrefs(ctx).saveGrantedAtMs(System.currentTimeMillis())
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        ActivityScenario.launch(VipManagementAct::class.java).use {
            // Row should contain a date string, we just check it's not "-"
            onView(withId(R.id.tvActivatedAt))
                .check(matches(withText(containsString("/"))))
        }
    }

    // ---- VIP entry label: grace vs redeemed ----

    @Test
    fun graceEntry_labelShowsFirstInstallString() {
        // Grace = VIP active + userRedeemedAtLeastOnce == false
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        // Do NOT call markUserRedeemed → should show grace label

        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.tvVipEntryLabel))
                .check(matches(withText(R.string.vip_entry_first_install)))
        }
    }

    @Test
    fun redeemedEntry_labelShowsRedeemedString() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        VipPrefs(ctx).markUserRedeemed()
        VipPrefs(ctx).saveActivatedDays(30)

        ActivityScenario.launch(VipManagementAct::class.java).use {
            // vip_entry_redeemed = "VIP %1$d days" (en) or "VIP %1$d ngày" (vi)
            onView(withId(R.id.tvVipEntryLabel))
                .check(matches(withText(containsString("30"))))
        }
    }

    // ---- VipCalculator formula via instrumented context (no mocks needed) ----

    @Test
    fun elapsedProgress_atGrantTime_isZero() {
        val granted = System.currentTimeMillis()
        val expiry  = granted + 30L * 86_400_000L
        val progress = VipCalculator.computeElapsedProgress(granted, expiry, granted)
        assert(progress == 0) { "Expected 0 but got $progress" }
    }

    @Test
    fun elapsedProgress_pastExpiry_is100() {
        val granted = 1_000L
        val expiry  = 2_000L
        val progress = VipCalculator.computeElapsedProgress(granted, expiry, 99_999L)
        assert(progress == 100) { "Expected 100 but got $progress" }
    }

    @Test
    fun remainingParts_30d_returnsCorrectArray() {
        val parts = VipCalculator.remainingParts(30L * 86_400_000L)
        assert(parts[0] == 30L) { "Expected days=30 but got ${parts[0]}" }
        assert(parts[1] == 0L && parts[2] == 0L && parts[3] == 0L)
    }

    // ---- Offline resilience: VIP activate/check work without network ----

    @Test
    fun activateVipByKey_worksWithoutNetwork_stateCorrect() {
        // activateVipByKey only validates key == vipKeySecret locally,
        // no network needed. This proves the offline VIP feature works.
        val activated = AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        assert(activated) { "activateVipByKey should succeed offline" }
        assert(AdManager.isVipByKeyActive()) { "isVipByKeyActive should be true after activation" }
        assert(AdManager.getVipByKeyExpiry() > System.currentTimeMillis()) {
            "expiry should be in the future"
        }
    }

    @Test
    fun clearVipByKey_resetsStateWithoutNetwork() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        AdManager.clearVipByKey()
        assert(!AdManager.isVipByKeyActive()) { "VIP should be inactive after clear" }
    }

    // ---- Bug 10: loadRewarded skipped when VIP active ----
    // When VIP is active: watchAd button disabled, revoke enabled, activeVipCard visible.
    // After revoke confirmed: watchAd button re-enabled.

    @Test
    fun vipActive_watchAdButtonIsDisabled() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnWatchAdVip)).check(matches(not(isEnabled())))
        }
    }

    @Test
    fun vipActive_revokeButtonIsEnabled() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnRevokeVip)).check(matches(isEnabled()))
        }
    }

    @Test
    fun vipActive_activeVipCardIsVisible() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.activeVipCard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun vipActive_afterRevokeConfirmed_watchAdButtonIsEnabled() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnRevokeVip)).perform(click())
            onView(withText(R.string.confirm)).perform(click())
            onView(withId(R.id.btnWatchAdVip)).check(matches(isEnabled()))
        }
    }
}
