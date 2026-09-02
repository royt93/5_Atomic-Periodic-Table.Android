package com.mckimquyen.atomicPeriodicTable.feature.exam

import com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser
import java.util.Collections
import java.util.Random

/**
 * Pure/Android-independent, same convention as VipCalculator/FlashcardScheduler/StreakCalculator.
 * Atomic mass lookup is injected via [massLookup] (backed by ElementWeightCache at the call site)
 * instead of depending on it directly, so this stays JVM-testable without an Android Context.
 */
object MolarMassQuestionGenerator {

    val COMMON_FORMULAS = listOf(
        "H2O", "CO2", "NaCl", "CH4", "NH3", "O2", "H2SO4", "HCl", "CaCO3", "C6H12O6",
        "NaOH", "KMnO4", "Fe2O3", "AgNO3", "Al2O3",
    )

    data class Question(val formula: String, val correctMass: Double, val choices: List<Double>)

    fun computeMolarMass(formula: String, massLookup: (String) -> Double?): Double? {
        val counts = ChemicalFormulaParser.parse(formula)
        var total = 0.0
        for ((symbol, count) in counts) {
            val mass = massLookup(symbol) ?: return null
            total += mass * count
        }
        return total
    }

    fun generate(random: Random, massLookup: (String) -> Double?): Question? {
        val formula = COMMON_FORMULAS[random.nextInt(COMMON_FORMULAS.size)]
        val correctMass = computeMolarMass(formula, massLookup) ?: return null
        val roundedCorrect = Math.round(correctMass * 100) / 100.0

        val wrongChoices = mutableSetOf<Double>()
        var attempts = 0
        while (wrongChoices.size < 3 && attempts < 100) {
            attempts++
            val deviation = (0.1 + random.nextDouble() * 0.2) * (if (random.nextBoolean()) 1 else -1)
            val wrong = Math.round(roundedCorrect * (1 + deviation) * 100) / 100.0
            if (wrong > 0 && wrong != roundedCorrect) {
                wrongChoices.add(wrong)
            }
        }
        // Fallback in the pathological case attempts run out before 3 unique values (never
        // observed with the current formula list, but keeps the contract of "always 4 choices").
        var filler = 1
        while (wrongChoices.size < 3) {
            val wrong = roundedCorrect + filler
            if (wrongChoices.add(wrong)) filler++
        }

        val choices = (wrongChoices + roundedCorrect).toMutableList()
        Collections.shuffle(choices, random)
        return Question(formula, roundedCorrect, choices)
    }
}
