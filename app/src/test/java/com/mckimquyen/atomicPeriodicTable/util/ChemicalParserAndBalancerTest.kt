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

        // Lowercase tests
        val lowercaseH2o = ChemicalFormulaParser.parse("h2o")
        assertEquals(2, lowercaseH2o["H"])
        assertEquals(1, lowercaseH2o["O"])

        val lowercaseNaCl = ChemicalFormulaParser.parse("nacl")
        assertEquals(1, lowercaseNaCl["Na"])
        assertEquals(1, lowercaseNaCl["Cl"])
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

        // Case-insensitive check
        val lowercaseWaterResult = ChemicalEquationBalancer.balance("h2 + o2 = h2o")
        assertNotNull(lowercaseWaterResult)
        assertEquals("2 H2 + O2 = 2 H2O", lowercaseWaterResult?.balancedString)
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

    // ===================================================================
    // Extended coverage: normalize() case handling
    // ===================================================================

    @Test
    fun testNormalize_basicCaseRecovery() {
        assertEquals("H2O", ChemicalFormulaParser.normalize("h2o"))
        assertEquals("NaCl", ChemicalFormulaParser.normalize("nacl"))
        assertEquals("HCl", ChemicalFormulaParser.normalize("hcl"))
        assertEquals("CH4", ChemicalFormulaParser.normalize("ch4"))
        assertEquals("CO2", ChemicalFormulaParser.normalize("co2"))
    }

    @Test
    fun testNormalize_preservesAlreadyCorrectFormulas() {
        assertEquals("Al2(SO4)3", ChemicalFormulaParser.normalize("Al2(SO4)3"))
        assertEquals("Ca(OH)2", ChemicalFormulaParser.normalize("Ca(OH)2"))
    }

    @Test
    fun testNormalize_stripsSpaces() {
        assertEquals("H2O", ChemicalFormulaParser.normalize(" h2o "))
    }

    @Test
    fun testNormalize_emptyReturnsEmpty() {
        assertEquals("", ChemicalFormulaParser.normalize(""))
        assertEquals("", ChemicalFormulaParser.normalize("   "))
    }

    // ===================================================================
    // Extended coverage: parser brackets, braces & errors
    // ===================================================================

    @Test
    fun testFormulaParser_BracesSupported() {
        val mgOH2 = ChemicalFormulaParser.parse("Mg{OH}2")
        assertEquals(1, mgOH2["Mg"])
        assertEquals(2, mgOH2["O"])
        assertEquals(2, mgOH2["H"])
    }

    @Test
    fun testFormulaParser_DeeplyNestedGroups() {
        // K4[Fe(CN)6] -> K4 Fe1 C6 N6
        val ferrocyanide = ChemicalFormulaParser.parse("K4[Fe(CN)6]")
        assertEquals(4, ferrocyanide["K"])
        assertEquals(1, ferrocyanide["Fe"])
        assertEquals(6, ferrocyanide["C"])
        assertEquals(6, ferrocyanide["N"])
    }

    @Test
    fun testFormulaParser_RepeatedElementsAccumulate() {
        // CH3COOH -> C2 H4 O2
        val aceticAcid = ChemicalFormulaParser.parse("CH3COOH")
        assertEquals(2, aceticAcid["C"])
        assertEquals(4, aceticAcid["H"])
        assertEquals(2, aceticAcid["O"])
    }

    @Test
    fun testFormulaParser_UnexpectedCharacterThrows() {
        try {
            ChemicalFormulaParser.parse("H2O!")
            fail("Should throw IllegalArgumentException for unexpected character")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun testFormulaParser_UnclosedOpeningBracketThrows() {
        try {
            ChemicalFormulaParser.parse("(H2O")
            fail("Should throw IllegalArgumentException for unclosed bracket")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    // ===================================================================
    // Extended coverage: balancer notation & coefficients
    // ===================================================================

    @Test
    fun testEquationBalancer_ArrowNotation() {
        val result = ChemicalEquationBalancer.balance("Fe + Cl2 -> FeCl3")
        assertNotNull(result)
        assertEquals("2 Fe + 3 Cl2 = 2 FeCl3", result?.balancedString)
    }

    @Test
    fun testEquationBalancer_AluminiumOxide() {
        // Al + O2 = Al2O3 -> 4 Al + 3 O2 = 2 Al2O3
        val result = ChemicalEquationBalancer.balance("Al + O2 = Al2O3")
        assertNotNull(result)
        assertEquals("4 Al + 3 O2 = 2 Al2O3", result?.balancedString)
        assertEquals(listOf(4L, 3L), result?.reactantCoefficients)
        assertEquals(listOf(2L), result?.productCoefficients)
    }

    @Test
    fun testNormalize_caseAwareSymbolBoundaries() {
        // Two uppercase letters are two separate elements: NH3 -> N + H + 3 (ammonia),
        // NOT the single 2-letter symbol "Nh" (Nihonium).
        assertEquals("NH3", ChemicalFormulaParser.normalize("NH3"))
        assertEquals("CO2", ChemicalFormulaParser.normalize("CO2"))
        // An uppercase followed by a lowercase IS a 2-letter symbol and is respected.
        assertEquals("Nh3", ChemicalFormulaParser.normalize("Nh3")) // Nihonium
        assertEquals("Co", ChemicalFormulaParser.normalize("Co"))   // Cobalt
        assertEquals("CO", ChemicalFormulaParser.normalize("CO"))   // Carbon + Oxygen
    }

    @Test
    fun testEquationBalancer_AmmoniaSynthesis() {
        // N2 + H2 = NH3 -> N2 + 3 H2 = 2 NH3 (regression test for the case-aware fix)
        val result = ChemicalEquationBalancer.balance("N2 + H2 = NH3")
        assertNotNull(result)
        assertEquals("N2 + 3 H2 = 2 NH3", result?.balancedString)
        assertEquals(listOf(1L, 3L), result?.reactantCoefficients)
        assertEquals(listOf(2L), result?.productCoefficients)
    }

    @Test
    fun testEquationBalancer_AlreadyMinimal() {
        // C + O2 = CO2 -> coefficients all 1, string unchanged
        val result = ChemicalEquationBalancer.balance("C + O2 = CO2")
        assertNotNull(result)
        assertEquals("C + O2 = CO2", result?.balancedString)
    }

    @Test
    fun testEquationBalancer_ComplexCombustion() {
        // C2H6 + O2 = CO2 + H2O -> 2 C2H6 + 7 O2 = 4 CO2 + 6 H2O
        val result = ChemicalEquationBalancer.balance("C2H6 + O2 = CO2 + H2O")
        assertNotNull(result)
        assertEquals("2 C2H6 + 7 O2 = 4 CO2 + 6 H2O", result?.balancedString)
    }

    @Test
    fun testEquationBalancer_ThreePartsRejected() {
        // More than one separator -> invalid
        assertNull(ChemicalEquationBalancer.balance("H2 = O2 = H2O"))
    }

    @Test
    fun testEquationBalancer_EmptySideRejected() {
        assertNull(ChemicalEquationBalancer.balance("= H2O"))
        assertNull(ChemicalEquationBalancer.balance("H2 + O2 ="))
    }
}
