package com.mckimquyen.atomicPeriodicTable.feature.exam

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class MolarMassQuestionGeneratorTest {

    // Fixed masses (rounded, real-world approximate) so expected totals are easy to hand-verify.
    private val massLookup: (String) -> Double? = { symbol ->
        when (symbol) {
            "H" -> 1.008
            "O" -> 15.999
            "C" -> 12.011
            "N" -> 14.007
            "Na" -> 22.990
            "Cl" -> 35.450
            else -> null
        }
    }

    @Test
    fun computeMolarMass_water_matchesHandCalculatedTotal() {
        val mass = MolarMassQuestionGenerator.computeMolarMass("H2O", massLookup)
        assertNotNull(mass)
        // 2*1.008 + 15.999 = 18.015
        assertEquals(18.015, mass!!, 0.001)
    }

    @Test
    fun computeMolarMass_sodiumChloride_matchesHandCalculatedTotal() {
        val mass = MolarMassQuestionGenerator.computeMolarMass("NaCl", massLookup)
        assertNotNull(mass)
        assertEquals(58.44, mass!!, 0.001)
    }

    @Test
    fun computeMolarMass_unknownSymbol_returnsNull() {
        // "Fe" isn't in this test's massLookup table.
        val mass = MolarMassQuestionGenerator.computeMolarMass("Fe2O3", massLookup)
        assertNull(mass)
    }

    @Test
    fun generate_withFullLookupTable_alwaysProducesFourDistinctChoicesIncludingCorrectAnswer() {
        val fullLookup: (String) -> Double? = { symbol ->
            when (symbol) {
                "H" -> 1.008; "O" -> 15.999; "C" -> 12.011; "N" -> 14.007
                "Na" -> 22.990; "Cl" -> 35.450; "S" -> 32.06; "Ca" -> 40.078
                "K" -> 39.098; "Mn" -> 54.938; "Fe" -> 55.845; "Ag" -> 107.868
                "Al" -> 26.982
                else -> null
            }
        }
        repeat(50) { seed ->
            val question = MolarMassQuestionGenerator.generate(Random(seed.toLong()), fullLookup)
            assertNotNull("seed=$seed", question)
            assertEquals("seed=$seed", 4, question!!.choices.size)
            assertEquals("seed=$seed", 4, question.choices.toSet().size) // all distinct
            assertTrue("seed=$seed: ${question.choices} must contain ${question.correctMass}", question.choices.contains(question.correctMass))
            question.choices.forEach { assertTrue("seed=$seed: choice $it must be positive", it > 0) }
        }
    }

    @Test
    fun generate_missingElementInLookup_returnsNull() {
        // massLookup that always fails simulates a formula the caller can't price.
        val question = MolarMassQuestionGenerator.generate(Random(1), { null })
        assertNull(question)
    }

    @Test
    fun generate_wrongChoicesDeviateFromCorrectAnswer() {
        val fullLookup: (String) -> Double? = { symbol -> if (symbol == "H") 1.008 else if (symbol == "O") 15.999 else null }
        val question = MolarMassQuestionGenerator.generate(Random(42), fullLookup)
        assertNotNull(question)
        val wrongChoices = question!!.choices.filter { it != question.correctMass }
        assertEquals(3, wrongChoices.size)
        wrongChoices.forEach { wrong ->
            val deviationRatio = kotlin.math.abs(wrong - question.correctMass) / question.correctMass
            assertTrue("deviation $deviationRatio should be meaningfully non-zero", deviationRatio > 0.01)
        }
    }
}
