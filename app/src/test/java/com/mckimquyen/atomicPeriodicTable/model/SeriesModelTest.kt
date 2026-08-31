package com.mckimquyen.atomicPeriodicTable.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesModelTest {

    // Regression guard for FIX-003: copper was listed with voltage -0.159, which is both
    // scientifically wrong (Cu2+/Cu standard reduction potential is positive) and broke
    // the ascending order of the electrochemical series.
    @Test
    fun testCopperVoltageIsPositive() {
        val series = ArrayList<Series>()
        SeriesModel.getList(series)
        val copper = series.first { it.name == "copper" }
        assertEquals("Cu", copper.short)
        assertTrue("Thế điện cực chuẩn của Cu2+/Cu phải dương, hiện là ${copper.voltage}", copper.voltage > 0)
    }

    @Test
    fun testSeriesIsSortedAscendingByVoltage() {
        val series = ArrayList<Series>()
        SeriesModel.getList(series)

        for (i in 1 until series.size) {
            val previous = series[i - 1]
            val current = series[i]
            assertTrue(
                "Dãy điện hoá không tăng dần: ${previous.name}(${previous.voltage}) -> ${current.name}(${current.voltage})",
                previous.voltage < current.voltage
            )
        }
    }
}
