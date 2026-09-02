package com.mckimquyen.atomicPeriodicTable.util

import com.mckimquyen.atomicPeriodicTable.pref.FontScalePref
import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleHelperTest {

    @Test
    fun small_mapsToBelowOneMultiplier() {
        assertEquals(0.85f, FontScaleHelper.multiplierFor(FontScalePref.SMALL), 0.0001f)
    }

    @Test
    fun default_mapsToNoOpMultiplier() {
        assertEquals(1.0f, FontScaleHelper.multiplierFor(FontScalePref.DEFAULT), 0.0001f)
    }

    @Test
    fun large_mapsToAboveOneMultiplier() {
        assertEquals(1.15f, FontScaleHelper.multiplierFor(FontScalePref.LARGE), 0.0001f)
    }

    @Test
    fun unknownValue_fallsBackToNoOpMultiplier() {
        assertEquals(1.0f, FontScaleHelper.multiplierFor(999), 0.0001f)
    }
}
