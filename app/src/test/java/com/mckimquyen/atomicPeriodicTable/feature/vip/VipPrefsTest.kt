package com.mckimquyen.atomicPeriodicTable.feature.vip

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VipPrefsTest {

    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockSp: SharedPreferences
    @Mock private lateinit var mockEditor: SharedPreferences.Editor

    private val store = mutableMapOf<String, Any?>()
    private lateinit var prefs: VipPrefs

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        whenever(mockEditor.putLong(any(), any())).doAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Long>(1); mockEditor
        }
        whenever(mockEditor.putInt(any(), any())).doAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Int>(1); mockEditor
        }
        whenever(mockEditor.putBoolean(any(), any())).doAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Boolean>(1); mockEditor
        }
        whenever(mockEditor.remove(any())).doAnswer { inv ->
            store.remove(inv.getArgument<String>(0)); mockEditor
        }
        whenever(mockEditor.apply()).doReturn(Unit)

        whenever(mockSp.edit()).doReturn(mockEditor)
        whenever(mockSp.getLong(any(), any())).doAnswer { inv ->
            (store[inv.getArgument(0)] as? Long) ?: inv.getArgument(1)
        }
        whenever(mockSp.getInt(any(), any())).doAnswer { inv ->
            (store[inv.getArgument(0)] as? Int) ?: inv.getArgument(1)
        }
        whenever(mockSp.getBoolean(any(), any())).doAnswer { inv ->
            (store[inv.getArgument(0)] as? Boolean) ?: inv.getArgument(1)
        }

        whenever(mockContext.getSharedPreferences(eq("vip_screen_prefs"), eq(Context.MODE_PRIVATE)))
            .doReturn(mockSp)

        prefs = VipPrefs(mockContext)
    }

    // ---- grantedAtMs ----

    @Test
    fun `getGrantedAtMs returns 0 before any save`() {
        assertEquals(0L, prefs.getGrantedAtMs())
    }

    @Test
    fun `saveGrantedAtMs and getGrantedAtMs round-trip`() {
        val ts = 1_700_000_000_000L
        prefs.saveGrantedAtMs(ts)
        assertEquals(ts, prefs.getGrantedAtMs())
    }

    @Test
    fun `clearGrantedAtMs resets to 0`() {
        prefs.saveGrantedAtMs(99_999L)
        prefs.clearGrantedAtMs()
        assertEquals(0L, prefs.getGrantedAtMs())
    }

    @Test
    fun `saveGrantedAtMs overwrites previous value`() {
        prefs.saveGrantedAtMs(1_000L)
        prefs.saveGrantedAtMs(2_000L)
        assertEquals(2_000L, prefs.getGrantedAtMs())
    }

    // ---- activatedDays ----

    @Test
    fun `getActivatedDays returns 0 before any save`() {
        assertEquals(0, prefs.getActivatedDays())
    }

    @Test
    fun `saveActivatedDays round-trip for 30 days`() {
        prefs.saveActivatedDays(30)
        assertEquals(30, prefs.getActivatedDays())
    }

    @Test
    fun `saveActivatedDays round-trip for 3 days`() {
        prefs.saveActivatedDays(3)
        assertEquals(3, prefs.getActivatedDays())
    }

    // ---- userRedeemed ----

    @Test
    fun `userRedeemedAtLeastOnce returns false before markUserRedeemed`() {
        assertFalse(prefs.userRedeemedAtLeastOnce())
    }

    @Test
    fun `markUserRedeemed sets flag to true`() {
        prefs.markUserRedeemed()
        assertTrue(prefs.userRedeemedAtLeastOnce())
    }

    @Test
    fun `clearUserRedeemed resets redeemed flag and days`() {
        prefs.markUserRedeemed()
        prefs.saveActivatedDays(30)
        prefs.clearUserRedeemed()
        assertFalse(prefs.userRedeemedAtLeastOnce())
        assertEquals(0, prefs.getActivatedDays())
    }

    @Test
    fun `clearUserRedeemed does not affect grantedAtMs`() {
        prefs.saveGrantedAtMs(12_345L)
        prefs.markUserRedeemed()
        prefs.clearUserRedeemed()
        assertEquals(12_345L, prefs.getGrantedAtMs())
    }
}
