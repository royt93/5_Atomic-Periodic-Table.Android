@file:OptIn(com.roy.sdkadbmob.InternalAdApi::class)

package com.mckimquyen.atomicPeriodicTable.act

import android.content.Context
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.setting.FavoritePageAct
import com.mckimquyen.atomicPeriodicTable.feature.vip.VipKeys
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.clearAppPreferencesForTest
import com.roy.sdkadbmob.resetVipActivationBackoffForTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests proving Bug 12 fix: banner is NOT added to bannerContainer
 * when VIP is active — checked for ElementInfoAct, SettingsAct, and FavoritePageAct.
 *
 * The gate `if (!AdManager.isVipByKeyActive()) AdManager.loadBanner(...)` is synchronous
 * (the guard runs in onCreate before any async callback), so childCount == 0 immediately
 * after onCreate is a reliable signal that the gate fired correctly.
 */
@RunWith(AndroidJUnit4::class)
class BannerVipGateTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        // vipRedeemCodes marks each code used-per-device permanently; clearVipByKey() only
        // resets the expiry, not that mark — reset the whole store so VipKeys.VIP_30D_KEY can
        // be redeemed again by the next @Test. Also reset the shared brute-force backoff.
        AdManager.clearAppPreferencesForTest(ctx)
        AdManager.resetVipActivationBackoffForTest()
    }

    // ---- ElementInfoAct ----

    @Test
    fun elementInfoAct_vipActive_bannerContainerHasNoChildren() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        // ElementSendAndLoad.getValue() defaults to "hydrogen" so no seeding needed.
        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<ViewGroup>(R.id.bannerContainer)
                assertEquals(
                    "bannerContainer must have no children when VIP is active (ElementInfoAct)",
                    0,
                    container.childCount
                )
            }
        }
    }

    // ---- SettingsAct ----

    @Test
    fun settingsAct_vipActive_bannerContainerHasNoChildren() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        ActivityScenario.launch(SettingsAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<ViewGroup>(R.id.bannerContainer)
                assertEquals(
                    "bannerContainer must have no children when VIP is active (SettingsAct)",
                    0,
                    container.childCount
                )
            }
        }
    }

    // ---- FavoritePageAct ----

    @Test
    fun favoritePageAct_vipActive_bannerContainerHasNoChildren() {
        AdManager.activateVipByKey(ctx, VipKeys.VIP_30D_KEY, 30)

        ActivityScenario.launch(FavoritePageAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<ViewGroup>(R.id.bannerContainer)
                assertEquals(
                    "bannerContainer must have no children when VIP is active (FavoritePageAct)",
                    0,
                    container.childCount
                )
            }
        }
    }
}
