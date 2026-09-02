package com.mckimquyen.atomicPeriodicTable.feature.compound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompoundMatcherTest {

    @Test
    fun findMatch_exactWaterComposition_matchesH2O() {
        assertEquals("H2O", CompoundMatcher.findMatch(listOf("H", "H", "O")))
    }

    @Test
    fun findMatch_orderDoesNotMatter() {
        assertEquals("H2O", CompoundMatcher.findMatch(listOf("O", "H", "H")))
    }

    @Test
    fun findMatch_missingAnAtom_doesNotMatch() {
        // Only 1 H and 1 O — H2O needs 2 H.
        assertNull(CompoundMatcher.findMatch(listOf("H", "O")))
    }

    @Test
    fun findMatch_extraAtom_doesNotMatch() {
        // 3 H and 1 O is not a curated formula either.
        assertNull(CompoundMatcher.findMatch(listOf("H", "H", "H", "O")))
    }

    @Test
    fun findMatch_emptySelection_returnsNull() {
        assertNull(CompoundMatcher.findMatch(emptyList()))
    }

    @Test
    fun findMatch_unrelatedElement_doesNotAccidentallyMatch() {
        assertNull(CompoundMatcher.findMatch(listOf("Fe", "Fe", "Fe")))
    }

    @Test
    fun findMatch_singleAtomFormula_matchesO2() {
        assertEquals("O2", CompoundMatcher.findMatch(listOf("O", "O")))
    }

    @Test
    fun findMatch_glucoseComposition_matchesC6H12O6() {
        val selection = List(6) { "C" } + List(12) { "H" } + List(6) { "O" }
        assertEquals("C6H12O6", CompoundMatcher.findMatch(selection))
    }

    @Test
    fun availableSymbols_containsCommonElementsFromCuratedFormulas() {
        val symbols = CompoundMatcher.availableSymbols
        assertTrue(symbols.contains("H"))
        assertTrue(symbols.contains("O"))
        assertTrue(symbols.contains("Na"))
        assertTrue(symbols.contains("Cl"))
    }

    @Test
    fun availableSymbols_hasNoDuplicates() {
        val symbols = CompoundMatcher.availableSymbols
        assertEquals(symbols.size, symbols.toSet().size)
    }

    @Test
    fun availableSymbols_everyCuratedFormulaIsBuildableFromTheList() {
        // Sanity check: the chip list must cover every element the 15 curated formulas use,
        // otherwise a formula would be permanently unreachable in the UI.
        val available = CompoundMatcher.availableSymbols.toSet()
        for (formula in com.mckimquyen.atomicPeriodicTable.feature.exam.MolarMassQuestionGenerator.COMMON_FORMULAS) {
            val requiredSymbols = com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser.parse(formula).keys
            assertTrue("formula $formula needs $requiredSymbols but chips only have $available", available.containsAll(requiredSymbols))
        }
    }
}
