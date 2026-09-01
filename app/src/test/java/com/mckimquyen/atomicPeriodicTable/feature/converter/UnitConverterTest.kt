package com.mckimquyen.atomicPeriodicTable.feature.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UnitConverterTest {

    // --- Pressure ---

    @Test
    fun pressure_sameUnit_isIdentity() {
        assertEquals(1.0, UnitConverter.convertPressure(1.0, PressureUnit.ATM, PressureUnit.ATM), 0.0001)
    }

    @Test
    fun pressure_atmToKpa() {
        assertEquals(101.325, UnitConverter.convertPressure(1.0, PressureUnit.ATM, PressureUnit.KPA), 0.001)
    }

    @Test
    fun pressure_atmToMmHg() {
        assertEquals(760.0, UnitConverter.convertPressure(1.0, PressureUnit.ATM, PressureUnit.MMHG), 0.5)
    }

    @Test
    fun pressure_atmToPsi() {
        assertEquals(14.6959, UnitConverter.convertPressure(1.0, PressureUnit.ATM, PressureUnit.PSI), 0.001)
    }

    @Test
    fun pressure_roundTrip_returnsOriginalValue() {
        val converted = UnitConverter.convertPressure(2.5, PressureUnit.PSI, PressureUnit.MMHG)
        val roundTripped = UnitConverter.convertPressure(converted, PressureUnit.MMHG, PressureUnit.PSI)
        assertEquals(2.5, roundTripped, 0.0001)
    }

    @Test
    fun pressure_zero_isAllowed() {
        assertEquals(0.0, UnitConverter.convertPressure(0.0, PressureUnit.ATM, PressureUnit.PSI), 0.0001)
    }

    @Test
    fun pressure_negative_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convertPressure(-1.0, PressureUnit.ATM, PressureUnit.KPA)
        }
    }

    // --- Mass ---

    @Test
    fun mass_kgToG() {
        assertEquals(1000.0, UnitConverter.convertMass(1.0, MassUnit.KG, MassUnit.G), 0.0001)
    }

    @Test
    fun mass_gToMg() {
        assertEquals(1000.0, UnitConverter.convertMass(1.0, MassUnit.G, MassUnit.MG), 0.0001)
    }

    @Test
    fun mass_mgToKg() {
        assertEquals(0.000001, UnitConverter.convertMass(1.0, MassUnit.MG, MassUnit.KG), 1e-9)
    }

    @Test
    fun mass_zero_isAllowed() {
        assertEquals(0.0, UnitConverter.convertMass(0.0, MassUnit.KG, MassUnit.MG), 0.0001)
    }

    @Test
    fun mass_negative_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convertMass(-5.0, MassUnit.G, MassUnit.KG)
        }
    }

    // --- Volume ---

    @Test
    fun volume_lToMl() {
        assertEquals(1000.0, UnitConverter.convertVolume(1.0, VolumeUnit.L, VolumeUnit.ML), 0.0001)
    }

    @Test
    fun volume_mlToL() {
        assertEquals(0.5, UnitConverter.convertVolume(500.0, VolumeUnit.ML, VolumeUnit.L), 0.0001)
    }

    @Test
    fun volume_zero_isAllowed() {
        assertEquals(0.0, UnitConverter.convertVolume(0.0, VolumeUnit.L, VolumeUnit.ML), 0.0001)
    }

    @Test
    fun volume_negative_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convertVolume(-1.0, VolumeUnit.L, VolumeUnit.ML)
        }
    }
}
