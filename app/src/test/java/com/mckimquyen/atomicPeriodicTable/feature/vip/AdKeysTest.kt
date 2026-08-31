package com.mckimquyen.atomicPeriodicTable.feature.vip

import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdKeysTest {

    // ---- private properties injected through BuildConfig ----

    @Test
    fun `VIP_SECRET is non-null and non-empty`() {
        assertNotNull(AdKeys.VIP_SECRET)
        assertTrue(AdKeys.VIP_SECRET.isNotEmpty())
    }

    @Test
    fun `VIP_SECRET is stored as its decoded value`() {
        assertFalse("VIP_SECRET should be decoded, not Base64", AdKeys.VIP_SECRET.endsWith("=="))
    }

    @Test
    fun `VIP_SECRET matches VipKeys 30D key`() {
        assertTrue(AdKeys.VIP_SECRET == VipKeys.VIP_30D_KEY)
    }

    @Test
    fun `VIP_SECRET contains special chars expected from spec`() {
        val key = AdKeys.VIP_SECRET
        val hasSpecialChar = key.any { !it.isLetterOrDigit() }
        assertTrue("Key must contain special chars per spec", hasSpecialChar)
    }

    // ---- pure compile-time constant checks — no Android API needed ----

    @Test
    fun `PRIVACY_POLICY_URL is non-empty`() {
        assertTrue(AdKeys.PRIVACY_POLICY_URL.isNotEmpty())
    }

    @Test
    fun `PRIVACY_POLICY_URL starts with https`() {
        assertTrue(AdKeys.PRIVACY_POLICY_URL.startsWith("https://"))
    }

    @Test
    fun `PRIVACY_POLICY_URL does not contain whitespace`() {
        assertFalse(
            "PRIVACY_POLICY_URL must not contain spaces or newlines",
            AdKeys.PRIVACY_POLICY_URL.any { it.isWhitespace() }
        )
    }

    @Test
    fun `PRIVACY_POLICY_URL contains a dot indicating a domain`() {
        assertTrue(
            "PRIVACY_POLICY_URL must look like a URL with a domain",
            AdKeys.PRIVACY_POLICY_URL.contains('.')
        )
    }
}
