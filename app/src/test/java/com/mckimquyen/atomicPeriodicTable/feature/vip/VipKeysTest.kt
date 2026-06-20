package com.mckimquyen.atomicPeriodicTable.feature.vip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VipKeysTest {

    // ---- decode sanity ----

    @Test
    fun `VIP_30D_KEY decodes to non-empty string`() {
        assertTrue(VipKeys.VIP_30D_KEY.isNotEmpty())
    }

    @Test
    fun `VIP_3D_KEY decodes to non-empty string`() {
        assertTrue(VipKeys.VIP_3D_KEY.isNotEmpty())
    }

    @Test
    fun `30D and 3D keys are different`() {
        assertNotEquals(VipKeys.VIP_30D_KEY, VipKeys.VIP_3D_KEY)
    }

    @Test
    fun `decoded keys are not Base64 strings themselves`() {
        // Base64 strings end with = padding; plain keys should not
        assertFalse(VipKeys.VIP_30D_KEY.endsWith("="))
        assertFalse(VipKeys.VIP_3D_KEY.endsWith("="))
    }

    // ---- lookupDays ----

    @Test
    fun `lookupDays returns 30 for exact 30D key`() {
        assertEquals(30, VipKeys.lookupDays(VipKeys.VIP_30D_KEY))
    }

    @Test
    fun `lookupDays returns 3 for exact 3D key`() {
        assertEquals(3, VipKeys.lookupDays(VipKeys.VIP_3D_KEY))
    }

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
    fun `lookupDays trims leading and trailing spaces`() {
        assertEquals(30, VipKeys.lookupDays("  ${VipKeys.VIP_30D_KEY}  "))
        assertEquals(3, VipKeys.lookupDays("\t${VipKeys.VIP_3D_KEY}\t"))
    }

    @Test
    fun `lookupDays is case-sensitive`() {
        // Keys contain special chars so lowercasing would break them anyway
        assertNull(VipKeys.lookupDays(VipKeys.VIP_30D_KEY.lowercase()))
    }
}
