package com.mckimquyen.atomicPeriodicTable.feature.vip

import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdKeysTest {

    @Test
    fun `VIP_SECRET decodes to non-null non-empty string`() {
        assertNotNull(AdKeys.VIP_SECRET)
        assertTrue(AdKeys.VIP_SECRET.isNotEmpty())
    }

    @Test
    fun `VIP_SECRET is not a Base64-encoded string itself (must be decoded)`() {
        // Plain key spec uses ! @ # — special chars not present in plain Base64 alphabet
        assertFalse("VIP_SECRET should be decoded, not Base64", AdKeys.VIP_SECRET.endsWith("=="))
    }

    @Test
    fun `VIP_SECRET matches VipKeys 30D key (single-secret design)`() {
        // Both AdKeys.VIP_SECRET and VipKeys.VIP_30D_KEY must decode from same Base64
        assertTrue(AdKeys.VIP_SECRET == VipKeys.VIP_30D_KEY)
    }

    @Test
    fun `VIP_SECRET contains special chars expected from spec`() {
        val key = AdKeys.VIP_SECRET
        val hasSpecialChar = key.any { !it.isLetterOrDigit() }
        assertTrue("Key must contain special chars per spec", hasSpecialChar)
    }

    @Test
    fun `PRIVACY_POLICY_URL is non-empty`() {
        // Compile-time BuildConfig check — not null at runtime
        assertTrue(AdKeys.PRIVACY_POLICY_URL.isNotEmpty())
    }

    @Test
    fun `PRIVACY_POLICY_URL starts with https`() {
        assertTrue(AdKeys.PRIVACY_POLICY_URL.startsWith("https://"))
    }
}
