package com.mckimquyen.atomicPeriodicTable.feature.vip

import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class AdKeysTest {

    // ---- tests that call VIP_SECRET → VipKeys.VIP_30D_KEY → android.util.Base64 ----
    // These require an Android device; android.util.Base64.decode() returns null in JVM.

    @Ignore("Requires Android device — VIP_SECRET delegates to VipKeys.VIP_30D_KEY which needs android.util.Base64")
    @Test
    fun `VIP_SECRET decodes to non-null non-empty string`() {
        assertNotNull(AdKeys.VIP_SECRET)
        assertTrue(AdKeys.VIP_SECRET.isNotEmpty())
    }

    @Ignore("Requires Android device — VIP_SECRET delegates to VipKeys.VIP_30D_KEY which needs android.util.Base64")
    @Test
    fun `VIP_SECRET is not a Base64-encoded string itself (must be decoded)`() {
        assertFalse("VIP_SECRET should be decoded, not Base64", AdKeys.VIP_SECRET.endsWith("=="))
    }

    @Ignore("Requires Android device — VIP_SECRET delegates to VipKeys.VIP_30D_KEY which needs android.util.Base64")
    @Test
    fun `VIP_SECRET matches VipKeys 30D key (single-secret design)`() {
        assertTrue(AdKeys.VIP_SECRET == VipKeys.VIP_30D_KEY)
    }

    @Ignore("Requires Android device — VIP_SECRET delegates to VipKeys.VIP_30D_KEY which needs android.util.Base64")
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
