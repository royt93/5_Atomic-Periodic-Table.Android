package com.mckimquyen.atomicPeriodicTable.feature.trends

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendsMapperTest {

    @Test
    fun mapX_minNumber_landsAtLeftPadding() {
        assertEquals(20f, TrendsMapper.mapX(1, 1, 118, 1000f, 20f), 0.01f)
    }

    @Test
    fun mapX_maxNumber_landsAtRightPadding() {
        assertEquals(980f, TrendsMapper.mapX(118, 1, 118, 1000f, 20f), 0.01f)
    }

    @Test
    fun mapX_midpoint_isHalfway() {
        val mid = TrendsMapper.mapX(60, 1, 119, 1000f, 0f) // (60-1)/(119-1) = 59/118 = 0.5
        assertEquals(500f, mid, 0.01f)
    }

    @Test
    fun mapY_minValue_landsAtBottomPadding() {
        assertEquals(980f, TrendsMapper.mapY(0.0, 0.0, 4.0, 1000f, 20f), 0.01f)
    }

    @Test
    fun mapY_maxValue_landsAtTopPadding() {
        assertEquals(20f, TrendsMapper.mapY(4.0, 0.0, 4.0, 1000f, 20f), 0.01f)
    }

    @Test
    fun mapY_isInverted_higherValueIsSmallerY() {
        val yLow = TrendsMapper.mapY(1.0, 0.0, 4.0, 1000f, 0f)
        val yHigh = TrendsMapper.mapY(3.0, 0.0, 4.0, 1000f, 0f)
        assertTrue("higher value ($yHigh) must draw above lower value ($yLow)", yHigh < yLow)
    }

    @Test
    fun nearestPointIndex_picksClosestPoint() {
        val points = listOf(0f to 0f, 100f to 100f, 200f to 200f)
        assertEquals(1, TrendsMapper.nearestPointIndex(90f, 110f, points))
    }

    @Test
    fun nearestPointIndex_exactMatchReturnsThatIndex() {
        val points = listOf(10f to 10f, 20f to 20f, 30f to 30f)
        assertEquals(2, TrendsMapper.nearestPointIndex(30f, 30f, points))
    }

    @Test
    fun nearestPointIndex_emptyList_returnsNull() {
        assertNull(TrendsMapper.nearestPointIndex(0f, 0f, emptyList()))
    }
}
