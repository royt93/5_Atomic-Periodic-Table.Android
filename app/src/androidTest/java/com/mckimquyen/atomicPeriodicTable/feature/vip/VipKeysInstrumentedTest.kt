package com.mckimquyen.atomicPeriodicTable.feature.vip

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for VIP values injected from the private BuildConfig.
 */
@RunWith(AndroidJUnit4::class)
class VipKeysInstrumentedTest {

    // ---- VIP_30D_KEY basic assertions ----

    @Test
    fun vip30DKey_isNotEmpty() {
        assertTrue("VIP_30D_KEY must not be empty", VipKeys.VIP_30D_KEY.isNotEmpty())
    }

    @Test
    fun vip30DKey_doesNotEndWithEquals() {
        assertFalse(
            "VIP_30D_KEY must not end with '=' (padding stripped by NO_WRAP)",
            VipKeys.VIP_30D_KEY.endsWith("=")
        )
    }

    // ---- VIP_3D_KEY basic assertions ----

    @Test
    fun vip3DKey_isNotEmpty() {
        assertTrue("VIP_3D_KEY must not be empty", VipKeys.VIP_3D_KEY.isNotEmpty())
    }

    // ---- Keys must be distinct ----

    @Test
    fun vip30DKey_and_vip3DKey_areDifferent() {
        assertNotEquals(
            "VIP_30D_KEY and VIP_3D_KEY must be different",
            VipKeys.VIP_30D_KEY,
            VipKeys.VIP_3D_KEY
        )
    }

    // ---- lookupDays exact matches ----

    @Test
    fun lookupDays_vip30DKey_returns30() {
        assertEquals(30, VipKeys.lookupDays(VipKeys.VIP_30D_KEY))
    }

    @Test
    fun lookupDays_vip3DKey_returns3() {
        assertEquals(3, VipKeys.lookupDays(VipKeys.VIP_3D_KEY))
    }

    @Test
    fun lookupDays_wrongKey_returnsNull() {
        assertNull(VipKeys.lookupDays("WRONG"))
    }

    // ---- trim behaviour ----

    @Test
    fun lookupDays_paddedWith30DKey_returns30() {
        val padded = "  ${VipKeys.VIP_30D_KEY}  "
        assertEquals(
            "lookupDays should trim whitespace before lookup",
            30,
            VipKeys.lookupDays(padded)
        )
    }

    // ---- AdKeys.VIP_SECRET delegation ----

    @Test
    fun adKeysVipSecret_equalsvip30DKey() {
        assertEquals(
            "AdKeys.VIP_SECRET must match VipKeys.VIP_30D_KEY",
            VipKeys.VIP_30D_KEY,
            AdKeys.VIP_SECRET
        )
    }

    @Test
    fun adKeysVipSecret_containsSpecialChars() {
        val secret = AdKeys.VIP_SECRET
        val specialChars = listOf('!', '@', '#', '$', '%', '^', '&', '*')
        val hasSpecial = specialChars.any { secret.contains(it) }
        assertTrue(
            "AdKeys.VIP_SECRET should contain at least one special character (!@#\$%^&*), was: $secret",
            hasSpecial
        )
    }
}
