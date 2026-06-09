package com.mckimquyen.atomicPeriodicTable.util

import org.junit.Assert.*
import org.junit.Test

class ChemicalParserAndBalancerTest {

    @Test
    fun testFormulaParser_BasicFormulas() {
        val h2o = ChemicalFormulaParser.parse("H2O")
        assertEquals(2, h2o["H"])
        assertEquals(1, h2o["O"])

        val co2 = ChemicalFormulaParser.parse("CO2")
        assertEquals(1, co2["C"])
        assertEquals(2, co2["O"])

        val nacl = ChemicalFormulaParser.parse("NaCl")
        assertEquals(1, nacl["Na"])
        assertEquals(1, nacl["Cl"])
    }

    @Test
    fun testFormulaParser_BracketsAndGroups() {
        val caOH2 = ChemicalFormulaParser.parse("Ca(OH)2")
        assertEquals(1, caOH2["Ca"])
        assertEquals(2, caOH2["O"])
        assertEquals(2, caOH2["H"])

        val al2SO43 = ChemicalFormulaParser.parse("Al2(SO4)3")
        assertEquals(2, al2SO43["Al"])
        assertEquals(3, al2SO43["S"])
        assertEquals(12, al2SO43["O"])

        val complex = ChemicalFormulaParser.parse("[Fe(CN)6]4")
        assertEquals(4, complex["Fe"])
        assertEquals(24, complex["C"])
        assertEquals(24, complex["N"])
    }

    @Test
    fun testFormulaParser_InvalidFormulas() {
        try {
            ChemicalFormulaParser.parse("H2O)")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            ChemicalFormulaParser.parse("Ca(OH")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }

        try {
            ChemicalFormulaParser.parse("")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun testEquationBalancer_SuccessCases() {
        // H2 + O2 = H2O -> 2 H2 + O2 = 2 H2O
        val waterResult = ChemicalEquationBalancer.balance("H2 + O2 = H2O")
        assertNotNull(waterResult)
        assertEquals("2 H2 + O2 = 2 H2O", waterResult?.balancedString)
        assertEquals(2L, waterResult?.reactantCoefficients?.get(0))
        assertEquals(1L, waterResult?.reactantCoefficients?.get(1))
        assertEquals(2L, waterResult?.productCoefficients?.get(0))

        // C3H8 + O2 = CO2 + H2O -> C3H8 + 5 O2 = 3 CO2 + 4 H2O
        val propaneResult = ChemicalEquationBalancer.balance("C3H8 + O2 = CO2 + H2O")
        assertNotNull(propaneResult)
        assertEquals("C3H8 + 5 O2 = 3 CO2 + 4 H2O", propaneResult?.balancedString)

        // Fe + Cl2 = FeCl3 -> 2 Fe + 3 Cl2 = 2 FeCl3
        val ironResult = ChemicalEquationBalancer.balance("Fe + Cl2 = FeCl3")
        assertNotNull(ironResult)
        assertEquals("2 Fe + 3 Cl2 = 2 FeCl3", ironResult?.balancedString)
    }

    @Test
    fun testEquationBalancer_FailureAndValidation() {
        // Elements mismatched: H2 + O2 = HCl
        val mismatch = ChemicalEquationBalancer.balance("H2 + O2 = HCl")
        assertNull(mismatch)

        // Invalid equation format (no equals or arrow)
        val badFormat = ChemicalEquationBalancer.balance("H2 + O2 + H2O")
        assertNull(badFormat)

        // Unbalanceable: H2O = CO2
        val unbalanceable = ChemicalEquationBalancer.balance("H2O = CO2")
        assertNull(unbalanceable)
    }
}
