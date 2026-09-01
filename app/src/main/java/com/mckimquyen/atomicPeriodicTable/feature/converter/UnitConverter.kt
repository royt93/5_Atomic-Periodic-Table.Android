package com.mckimquyen.atomicPeriodicTable.feature.converter

/** Factor to convert 1 unit into the group's base unit (Pascal / gram / liter). */
enum class PressureUnit(val toBase: Double, val label: String) {
    ATM(101_325.0, "atm"),
    KPA(1_000.0, "kPa"),
    MMHG(133.322, "mmHg"),
    PSI(6_894.76, "psi"),
}

enum class MassUnit(val toBase: Double, val label: String) {
    KG(1_000.0, "kg"),
    G(1.0, "g"),
    MG(0.001, "mg"),
}

enum class VolumeUnit(val toBase: Double, val label: String) {
    L(1.0, "L"),
    ML(0.001, "mL"),
}

/**
 * Pure/Android-independent unit conversion (pressure/mass/volume), same convention as
 * VipCalculator/FlashcardScheduler/TrendsMapper — JVM-testable without a Context. These are
 * physical magnitudes (not a signed scale like temperature), so negative input is rejected.
 */
object UnitConverter {
    fun convertPressure(value: Double, from: PressureUnit, to: PressureUnit): Double {
        require(value >= 0) { "Pressure cannot be negative" }
        return value * from.toBase / to.toBase
    }

    fun convertMass(value: Double, from: MassUnit, to: MassUnit): Double {
        require(value >= 0) { "Mass cannot be negative" }
        return value * from.toBase / to.toBase
    }

    fun convertVolume(value: Double, from: VolumeUnit, to: VolumeUnit): Double {
        require(value >= 0) { "Volume cannot be negative" }
        return value * from.toBase / to.toBase
    }
}
