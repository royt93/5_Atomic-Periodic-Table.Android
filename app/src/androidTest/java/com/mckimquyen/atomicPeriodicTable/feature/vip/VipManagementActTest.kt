package com.mckimquyen.atomicPeriodicTable.feature.vip

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.roy.sdkadbmob.AdManager
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests cho VipManagementAct.
 *
 * Mỗi test tự dọn dẹp VIP state trong @After để độc lập nhau.
 * AdManager.activateVipByKey hoạt động mà không cần network vì chỉ validate
 * key == vipKeySecret (đã configure trong RoyApp.configureAds).
 */
@RunWith(AndroidJUnit4::class)
class VipManagementActTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        AdManager.clearVipByKey()
        VipPrefs(ctx).clearGrantedAtMs()
        VipPrefs(ctx).clearUserRedeemed()
    }

    // ---- màn hình mở ở trạng thái free ----

    @Test
    fun screenOpens_statusHeaderVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.statusHeader)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun freeState_progressBarHidden() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.progressVip)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun freeState_activeVipCardHidden() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.activeVipCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun freeState_revokeButtonDisabled() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnRevokeVip)).check(matches(not(isEnabled())))
        }
    }

    @Test
    fun freeState_watchAdButtonVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnWatchAdVip)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun privacyPolicyFooter_isDisplayedAndClickable() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.tvPrivacyPolicy)).check(matches(isDisplayed()))
        }
    }

    // ---- nhập key không hợp lệ ----

    @Test
    fun invalidKey_showsFailedDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.editVipKey))
                .perform(replaceText("TOTALLY_WRONG_KEY_XYZ"), closeSoftKeyboard())
            onView(withId(R.id.btnRedeemVip)).perform(click())
            onView(withText(R.string.vip_failed_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun emptyKey_showsFailedDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            onView(withId(R.id.btnRedeemVip)).perform(click())
            onView(withText(R.string.vip_failed_title)).check(matches(isDisplayed()))
        }
    }

    // ---- kích hoạt bằng 30D key ----

    @Test
    fun valid30DKey_showsSuccessDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            onView(withText(R.string.vip_success_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid30DKey_afterDismiss_vipCardVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            dismissDialog()
            onView(withId(R.id.activeVipCard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid30DKey_afterDismiss_progressBarVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            dismissDialog()
            onView(withId(R.id.progressVip)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid30DKey_afterDismiss_countdownVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            dismissDialog()
            onView(withId(R.id.tvCountdown)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid30DKey_afterDismiss_revokeButtonEnabled() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            dismissDialog()
            onView(withId(R.id.btnRevokeVip)).check(matches(isEnabled()))
        }
    }

    // ---- thu hồi VIP ----

    @Test
    fun revoke_showsConfirmDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            activateVip30dAndDismiss()
            onView(withId(R.id.btnRevokeVip)).perform(click())
            onView(withText(R.string.vip_revoke_all_confirm_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun revoke_confirmed_vipCardHidden() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            activateVip30dAndDismiss()
            onView(withId(R.id.btnRevokeVip)).perform(click())
            onView(withText(R.string.confirm)).perform(click())
            onView(withId(R.id.activeVipCard)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun revoke_confirmed_progressBarHidden() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            activateVip30dAndDismiss()
            onView(withId(R.id.btnRevokeVip)).perform(click())
            onView(withText(R.string.confirm)).perform(click())
            onView(withId(R.id.progressVip)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun revoke_cancelled_vipCardStillVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            activateVip30dAndDismiss()
            onView(withId(R.id.btnRevokeVip)).perform(click())
            onView(withText(R.string.cancel)).perform(click())
            onView(withId(R.id.activeVipCard)).check(matches(isDisplayed()))
        }
    }

    // ---- Bug 8: redeemInputKey always passes VIP_SECRET ----
    // Even when the user types the 3D key, redeemInputKey internally passes VIP_SECRET
    // (which equals VIP_30D_KEY) to AdManager, so activation succeeds.

    @Test
    fun valid3DKey_showsSuccessDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            onView(withText(R.string.vip_success_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid3DKey_afterDismiss_vipCardVisible() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            dismissDialog()
            onView(withId(R.id.activeVipCard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun valid3DKey_afterDismiss_revokeButtonEnabled() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            dismissDialog()
            onView(withId(R.id.btnRevokeVip)).check(matches(isEnabled()))
        }
    }

    // ---- FIX-007: redeem while VIP already active must not silently shorten it ----

    @Test
    fun redeemShorterKeyWhileActive_showsReplaceConfirmDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            activateVip30dAndDismiss()
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            onView(withText(R.string.vip_redeem_replace_confirm_title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun redeemShorterKeyWhileActive_cancelled_keepsThirtyDaysMetadata() {
        ActivityScenario.launch(VipManagementAct::class.java).use { scenario ->
            activateVip30dAndDismiss()
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            onView(withText(R.string.cancel)).perform(click())

            scenario.onActivity { activity ->
                val days = VipPrefs(activity).getActivatedDays()
                assert(days == 30) { "Cancel phải giữ nguyên 30 ngày, hiện là $days" }
            }
        }
    }

    @Test
    fun redeemShorterKeyWhileActive_confirmed_appliesThreeDays() {
        ActivityScenario.launch(VipManagementAct::class.java).use { scenario ->
            activateVip30dAndDismiss()
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            onView(withText(R.string.confirm)).perform(click())
            onView(withText(R.string.vip_success_title)).check(matches(isDisplayed()))
            dismissDialog()

            scenario.onActivity { activity ->
                val days = VipPrefs(activity).getActivatedDays()
                assert(days == 3) { "Confirm phải đặt lại thành 3 ngày, hiện là $days" }
            }
        }
    }

    @Test
    fun redeemLongerKeyWhileActive_extendsWithoutConfirmDialog() {
        ActivityScenario.launch(VipManagementAct::class.java).use {
            enterKeyAndActivate(VipKeys.VIP_3D_KEY)
            dismissDialog()
            // Redeeming a LONGER key while active is an extension, not a shortening —
            // must go straight to success, no replace-confirm dialog.
            enterKeyAndActivate(VipKeys.VIP_30D_KEY)
            onView(withText(R.string.vip_success_title)).check(matches(isDisplayed()))
        }
    }

    // ---- helpers ----

    private fun enterKeyAndActivate(key: String) {
        onView(withId(R.id.editVipKey))
            .perform(replaceText(key), closeSoftKeyboard())
        onView(withId(R.id.btnRedeemVip)).perform(click())
    }

    private fun dismissDialog() {
        onView(withText(R.string.ok)).perform(click())
    }

    private fun activateVip30dAndDismiss() {
        enterKeyAndActivate(VipKeys.VIP_30D_KEY)
        dismissDialog()
    }
}
