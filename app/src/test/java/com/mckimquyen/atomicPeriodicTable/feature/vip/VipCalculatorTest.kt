package com.mckimquyen.atomicPeriodicTable.feature.vip

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class VipCalculatorTest {

    // ========================================================
    // computeElapsedProgress
    // ========================================================

    @Test
    fun `progress is 0 at exact grant time`() {
        val granted = 1_000_000L
        val expiry  = granted + 30L * 86_400_000L
        assertEquals(0, VipCalculator.computeElapsedProgress(granted, expiry, granted))
    }

    @Test
    fun `progress is 100 at exact expiry`() {
        val granted = 1_000_000L
        val expiry  = granted + 30L * 86_400_000L
        assertEquals(100, VipCalculator.computeElapsedProgress(granted, expiry, expiry))
    }

    @Test
    fun `progress is 50 at midpoint of 30d VIP`() {
        val granted = 0L
        val expiry  = 30L * 86_400_000L
        assertEquals(50, VipCalculator.computeElapsedProgress(granted, expiry, expiry / 2))
    }

    @Test
    fun `progress clamped to 0 when now is before grant (clock skew)`() {
        val granted = 2_000_000L
        val expiry  = granted + 86_400_000L
        assertEquals(0, VipCalculator.computeElapsedProgress(granted, expiry, granted - 1L))
    }

    @Test
    fun `progress clamped to 100 when now is after expiry`() {
        val granted = 1_000_000L
        val expiry  = granted + 86_400_000L
        assertEquals(100, VipCalculator.computeElapsedProgress(granted, expiry, expiry + 9999L))
    }

    @Test
    fun `progress returns 100 when total is 0 (grant equals expiry)`() {
        assertEquals(100, VipCalculator.computeElapsedProgress(5000L, 5000L, 5000L))
    }

    @Test
    fun `progress returns 100 when expiry is before grant (bad data)`() {
        assertEquals(100, VipCalculator.computeElapsedProgress(9000L, 1000L, 5000L))
    }

    @Test
    fun `progress at T+1d of 30d VIP is approximately 3 percent`() {
        val granted = 0L
        val expiry  = 30L * 86_400_000L
        val after1d = 86_400_000L
        val result  = VipCalculator.computeElapsedProgress(granted, expiry, after1d)
        assertEquals(3, result)
    }

    @Test
    fun `progress at T+15d of 30d VIP is 50 percent`() {
        val granted = 0L
        val expiry  = 30L * 86_400_000L
        val mid     = 15L * 86_400_000L
        assertEquals(50, VipCalculator.computeElapsedProgress(granted, expiry, mid))
    }

    // ========================================================
    // remainingParts — [days, hours, minutes, seconds]
    // ========================================================

    @Test
    fun `zero ms returns all zeros`() {
        assertArrayEquals(longArrayOf(0, 0, 0, 0), VipCalculator.remainingParts(0L))
    }

    @Test
    fun `negative ms treated as zero`() {
        assertArrayEquals(longArrayOf(0, 0, 0, 0), VipCalculator.remainingParts(-1_000L))
    }

    @Test
    fun `exactly 30 days`() {
        val ms = 30L * 86_400_000L
        assertArrayEquals(longArrayOf(30, 0, 0, 0), VipCalculator.remainingParts(ms))
    }

    @Test
    fun `1d 2h 3m 4s`() {
        val ms = 86_400_000L + 2 * 3_600_000L + 3 * 60_000L + 4_000L
        assertArrayEquals(longArrayOf(1, 2, 3, 4), VipCalculator.remainingParts(ms))
    }

    @Test
    fun `hours wrap at 24 — 25 hours equals 1d 1h`() {
        val ms = 25L * 3_600_000L
        assertArrayEquals(longArrayOf(1, 1, 0, 0), VipCalculator.remainingParts(ms))
    }

    @Test
    fun `minutes wrap at 60 — 61 minutes equals 0d 1h 1m`() {
        val ms = 61L * 60_000L
        assertArrayEquals(longArrayOf(0, 1, 1, 0), VipCalculator.remainingParts(ms))
    }

    @Test
    fun `seconds wrap at 60 — 61 seconds equals 0d 0h 1m 1s`() {
        val ms = 61L * 1_000L
        assertArrayEquals(longArrayOf(0, 0, 1, 1), VipCalculator.remainingParts(ms))
    }

    @Test
    fun `exactly 1 second`() {
        assertArrayEquals(longArrayOf(0, 0, 0, 1), VipCalculator.remainingParts(1_000L))
    }

    @Test
    fun `3 days VIP at T+0 shows 3 days`() {
        val ms = 3L * 86_400_000L
        assertArrayEquals(longArrayOf(3, 0, 0, 0), VipCalculator.remainingParts(ms))
    }

    // ========================================================
    // resolveGrantedAtMs (FIX-030)
    // ========================================================

    @Test
    fun `resolveGrantedAtMs returns the stored value when known`() {
        assertEquals(1_000_000L, VipCalculator.resolveGrantedAtMs(1_000_000L))
    }

    @Test
    fun `resolveGrantedAtMs returns 0 (unknown) instead of fabricating a 24h-ago guess`() {
        assertEquals(0L, VipCalculator.resolveGrantedAtMs(0L))
        assertEquals(0L, VipCalculator.resolveGrantedAtMs(-1L))
    }
}
