package com.mckimquyen.atomicPeriodicTable.feature.vip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class VipKeysTest {

    // ---- decode sanity — requires android.util.Base64 (Android device only) ----

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `VIP_30D_KEY decodes to non-empty string`() {
        assertTrue(VipKeys.VIP_30D_KEY.isNotEmpty())
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `VIP_3D_KEY decodes to non-empty string`() {
        assertTrue(VipKeys.VIP_3D_KEY.isNotEmpty())
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `30D and 3D keys are different`() {
        assertNotEquals(VipKeys.VIP_30D_KEY, VipKeys.VIP_3D_KEY)
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `decoded keys are not Base64 strings themselves`() {
        assertFalse(VipKeys.VIP_30D_KEY.endsWith("="))
        assertFalse(VipKeys.VIP_3D_KEY.endsWith("="))
    }

    // ---- lookupDays — uses VIP_30D_KEY / VIP_3D_KEY (Android device only) ----

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `lookupDays returns 30 for exact 30D key`() {
        assertEquals(30, VipKeys.lookupDays(VipKeys.VIP_30D_KEY))
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `lookupDays returns 3 for exact 3D key`() {
        assertEquals(3, VipKeys.lookupDays(VipKeys.VIP_3D_KEY))
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `lookupDays trims leading and trailing spaces`() {
        assertEquals(30, VipKeys.lookupDays("  ${VipKeys.VIP_30D_KEY}  "))
        assertEquals(3, VipKeys.lookupDays("\t${VipKeys.VIP_3D_KEY}\t"))
    }

    @Ignore("Requires Android device — covered by VipKeysInstrumentedTest")
    @Test
    fun `lookupDays is case-sensitive`() {
        assertNull(VipKeys.lookupDays(VipKeys.VIP_30D_KEY.lowercase()))
    }

    // ---- lookupDays pure-Kotlin paths — no Android API needed ----

    @Test
    fun `lookupDays returns null for unknown key`() {
        assertNull(VipKeys.lookupDays("TOTALLY_WRONG_KEY_XYZ_123"))
    }

    @Test
    fun `lookupDays returns null for empty string`() {
        assertNull(VipKeys.lookupDays(""))
    }

    @Test
    fun `lookupDays returns null for blank spaces only`() {
        assertNull(VipKeys.lookupDays("   "))
    }

    @Test
    fun `lookupDays returns null for key with only digits`() {
        assertNull(VipKeys.lookupDays("123456789"))
    }

    @Test
    fun `lookupDays returns null for key with only special chars`() {
        assertNull(VipKeys.lookupDays("!@#\$%^&*()"))
    }

    @Test
    fun `lookupDays returns null for very long random string`() {
        val longKey = "A".repeat(200)
        assertNull(VipKeys.lookupDays(longKey))
    }

    @Test
    fun `lookupDays trims input before lookup — trim does not change empty string`() {
        // Verify trim() on empty string is safe and still returns null
        assertNull(VipKeys.lookupDays("".trim()))
    }

    @Test
    fun `lookupDays trims input before lookup — spaces-only trims to empty`() {
        // "   ".trim() == "" which is not a valid key
        assertNull(VipKeys.lookupDays("   ".trim()))
    }
}
