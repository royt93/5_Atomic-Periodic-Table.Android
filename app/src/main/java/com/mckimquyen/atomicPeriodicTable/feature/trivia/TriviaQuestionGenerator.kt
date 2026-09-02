package com.mckimquyen.atomicPeriodicTable.feature.trivia

import java.util.Collections
import java.util.Random

/**
 * Pure/Android-independent, same convention as MolarMassQuestionGenerator: fact/name lookups
 * are injected instead of calling ElementWeightCache directly, so this stays JVM-testable.
 * Works on element SYMBOLS (not display names) — matching every other feature in this codebase
 * (CategoryFilter, ElementFilter) — translation to a localized display name happens at the
 * Android call site.
 */
object TriviaQuestionGenerator {

    data class Question(val fact: String, val correctSymbol: String, val choices: List<String>)

    /** "Which element does this fact describe?" — [correctSymbol] is always among [choices]. */
    fun generate(
        correctSymbol: String,
        allSymbols: List<String>,
        factLookup: (String) -> String?,
        random: Random,
    ): Question? {
        val fact = factLookup(correctSymbol)?.takeIf { it.isNotBlank() } ?: return null

        val distractorPool = allSymbols.filter { it != correctSymbol }.toMutableList()
        if (distractorPool.size < 3) return null
        Collections.shuffle(distractorPool, random)

        val choices = (distractorPool.take(3) + correctSymbol).toMutableList()
        Collections.shuffle(choices, random)

        return Question(fact, correctSymbol, choices)
    }
}
