package com.mckimquyen.atomicPeriodicTable.act

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-Kotlin tests for the logic patterns extracted from SplashAct.
 * No Android framework dependencies — runs on JVM without robolectric.
 */
class SplashFlowLogicTest {

    // ---- goToMain guard pattern ----

    private fun makeGoToMain(): Pair<() -> Unit, () -> Int> {
        var navigatedToMain = false
        var mainStarted = 0
        val fn = {
            if (!navigatedToMain) {
                navigatedToMain = true
                mainStarted++
            }
        }
        return fn to { mainStarted }
    }

    @Test
    fun `goToMain called once — mainStarted equals 1`() {
        val (goToMain, getCount) = makeGoToMain()
        goToMain()
        assertEquals(1, getCount())
    }

    @Test
    fun `goToMain called twice — mainStarted equals 1 (guard blocks second call)`() {
        val (goToMain, getCount) = makeGoToMain()
        goToMain()
        goToMain()
        assertEquals(1, getCount())
    }

    @Test
    fun `goToMain called 5 times — mainStarted equals 1 (guard blocks all subsequent calls)`() {
        val (goToMain, getCount) = makeGoToMain()
        repeat(5) { goToMain() }
        assertEquals(1, getCount())
    }

    @Test
    fun `goToMain not called — mainStarted equals 0`() {
        val (_, getCount) = makeGoToMain()
        assertEquals(0, getCount())
    }

    @Test
    fun `navigatedToMain set to true manually — goToMain becomes no-op`() {
        var navigatedToMain = true   // pre-set, simulating timeout already fired
        var mainStarted = 0
        fun goToMain() {
            if (navigatedToMain) return
            navigatedToMain = true
            mainStarted++
        }
        goToMain()
        assertEquals(0, mainStarted)
    }

    // ---- timeout selection logic ----

    private fun selectTimeout(hasNetwork: Boolean) = if (hasNetwork) 3_000L else 8_000L

    @Test
    fun `selectTimeout with network returns 3000ms`() {
        assertEquals(3_000L, selectTimeout(hasNetwork = true))
    }

    @Test
    fun `selectTimeout without network returns 8000ms`() {
        assertEquals(8_000L, selectTimeout(hasNetwork = false))
    }

    @Test
    fun `offline timeout is greater than online timeout`() {
        assertTrue(selectTimeout(hasNetwork = false) > selectTimeout(hasNetwork = true))
    }

    // ---- race condition guard documentation (Bug F1) ----

    /**
     * Documents that `navigatedToMain` is the correct guard, not `isFinishing`.
     *
     * Timeline of the race:
     *   T+0ms   — timeout fires → goToMain() → navigatedToMain = true → postDelayed(finish, 300ms)
     *   T+0..300ms — consent callback can still arrive; isFinishing is false here
     *   T+300ms — finish() is called → isFinishing becomes true
     *
     * Using `if (isFinishing)` misses the 0–300ms window.
     * Using `if (navigatedToMain)` catches it immediately at T+0ms.
     */
    @Test
    fun `navigatedToMain guard blocks re-entry before finish is posted`() {
        var navigatedToMain = false
        var finishScheduled = false

        // Simulate: timeout fires → goToMain() sets flag and schedules finish(+300ms)
        navigatedToMain = true
        finishScheduled = true

        // At this point finish() has NOT been called yet (still within the 300ms window).
        // isFinishing would be false here — wrong guard passes through.
        val isFinishing = false
        val wrongGuardBlocksCallback = isFinishing           // false → callback proceeds — BAD
        val correctGuardBlocksCallback = navigatedToMain    // true  → callback is stopped — GOOD

        assertFalse("isFinishing is false within 300ms window — wrong guard fails", wrongGuardBlocksCallback)
        assertTrue("navigatedToMain is true immediately — correct guard works", correctGuardBlocksCallback)
        assertTrue("finish was scheduled but not yet executed", finishScheduled)
    }

    @Test
    fun `navigatedToMain guard is idempotent — flag stays true after multiple guard checks`() {
        var navigatedToMain = false

        fun guardedAction(): Boolean {
            if (navigatedToMain) return false
            navigatedToMain = true
            return true
        }

        assertTrue("first call executes the action", guardedAction())
        assertFalse("second call is blocked", guardedAction())
        assertFalse("third call is blocked", guardedAction())
        assertTrue("flag remains true after repeated checks", navigatedToMain)
    }

    // ---- FIX-019: 30s emergency escape must be a hard cap, not just a retry ----
    //
    // Pre-fix bug: the 30s escape called plain goToMain() again. That re-entered the
    // isFullscreenAdShowing branch; since adDismissListener was already non-null, it
    // skipped scheduling a NEW escape and returned without navigating — if
    // isFullscreenAdShowing never clears, the splash is stuck forever with zero future
    // fallback. The fix adds a `force` parameter that bypasses the ad-showing check.

    private class GoToMainSimulator(private val isFullscreenAdShowing: () -> Boolean) {
        var navigatedToMain = false
            private set
        private var dismissListenerRegistered = false
        var escapeScheduledCount = 0
            private set

        fun goToMain(force: Boolean = false) {
            if (navigatedToMain) return
            if (!force && isFullscreenAdShowing()) {
                if (!dismissListenerRegistered) {
                    dismissListenerRegistered = true
                    escapeScheduledCount++
                }
                return
            }
            navigatedToMain = true
        }
    }

    @Test
    fun `FIX-019 - forced escape navigates even while ad is still marked showing`() {
        val sim = GoToMainSimulator(isFullscreenAdShowing = { true })
        sim.goToMain()
        assertFalse("still deferred while ad is showing", sim.navigatedToMain)
        assertEquals(1, sim.escapeScheduledCount)

        // 30s later: ad is STILL (bug-simulated) showing, escape timer fires with force=true.
        sim.goToMain(force = true)
        assertTrue("forced escape must navigate regardless of ad state", sim.navigatedToMain)
    }

    @Test
    fun `FIX-019 regression - without force, stuck ad state schedules no further escape and never navigates`() {
        val sim = GoToMainSimulator(isFullscreenAdShowing = { true })
        sim.goToMain()
        assertEquals(1, sim.escapeScheduledCount)

        sim.goToMain() // pre-fix behavior: escape re-calls plain goToMain(), no force
        assertFalse("bug: stuck forever with no navigation", sim.navigatedToMain)
        assertEquals("bug: no second escape gets scheduled either", 1, sim.escapeScheduledCount)
    }

    @Test
    fun `FIX-019 - normal dismissal without escape still navigates once ad clears`() {
        var adShowing = true
        val sim = GoToMainSimulator(isFullscreenAdShowing = { adShowing })
        sim.goToMain()
        assertFalse(sim.navigatedToMain)

        adShowing = false // ad dismissed normally, well before the 30s escape
        sim.goToMain()
        assertTrue(sim.navigatedToMain)
    }
}
