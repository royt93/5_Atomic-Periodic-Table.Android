package com.mckimquyen.atomicPeriodicTable.act

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression guard for the bug caught during ad-config audit: enabling
 * BuildConfig.IS_ENABLE_ADMOB for release (real production traffic) meant the old
 * `if (BuildConfig.IS_ENABLE_ADMOB)` gate on copyTestDeviceHashToClipboard() would fire for
 * every real user on every MainAct launch — silently overwriting their clipboard and showing
 * a Toast. Must require BOTH debug AND admob enabled, never admob alone.
 */
class MainActAdConfigTest {

    @Test
    fun `debug plus admob enabled captures hash`() {
        assertTrue(MainAct.shouldCaptureAdMobTestDeviceHash(isDebug = true, isEnableAdmob = true))
    }

    @Test
    fun `release with admob enabled does not capture hash`() {
        assertFalse(MainAct.shouldCaptureAdMobTestDeviceHash(isDebug = false, isEnableAdmob = true))
    }

    @Test
    fun `debug with admob disabled does not capture hash`() {
        assertFalse(MainAct.shouldCaptureAdMobTestDeviceHash(isDebug = true, isEnableAdmob = false))
    }

    @Test
    fun `release with admob disabled does not capture hash`() {
        assertFalse(MainAct.shouldCaptureAdMobTestDeviceHash(isDebug = false, isEnableAdmob = false))
    }
}
