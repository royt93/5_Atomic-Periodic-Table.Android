package com.mckimquyen.atomicPeriodicTable.feature.compound

import com.mckimquyen.atomicPeriodicTable.feature.exam.MolarMassQuestionGenerator
import com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser

/**
 * Pure/Android-independent, same convention as StreakCalculator/BadgeCalculator. Matches a
 * user-built multiset of element symbols against the curated compound list already used by
 * Practice Exam's molar-mass questions (MolarMassQuestionGenerator.COMMON_FORMULAS) — reused
 * as-is rather than moved to a neutral package, to avoid touching already-tested Quiz code for
 * what would be a pure package-naming concern.
 */
object CompoundMatcher {

    /** Every element symbol that appears in at least one curated formula — the chip list source. */
    val availableSymbols: List<String> by lazy {
        MolarMassQuestionGenerator.COMMON_FORMULAS
            .flatMap { ChemicalFormulaParser.parse(it).keys }
            .distinct()
            .sorted()
    }

    /** Returns the curated formula whose exact atom multiset matches [selectedSymbols], or null. */
    fun findMatch(selectedSymbols: List<String>): String? {
        if (selectedSymbols.isEmpty()) return null
        val selectedCounts = selectedSymbols.groupingBy { it }.eachCount()
        return MolarMassQuestionGenerator.COMMON_FORMULAS.firstOrNull { formula ->
            ChemicalFormulaParser.parse(formula) == selectedCounts
        }
    }
}
