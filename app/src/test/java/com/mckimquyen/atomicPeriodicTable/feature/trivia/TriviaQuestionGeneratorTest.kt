package com.mckimquyen.atomicPeriodicTable.feature.trivia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class TriviaQuestionGeneratorTest {

    private val allSymbols = listOf("H", "He", "Li", "Be", "B", "C")
    private val fact: (String) -> String? = { symbol -> if (symbol == "He") "A noble gas." else null }

    @Test
    fun generate_returnsFourDistinctChoices_includingTheCorrectAnswer() {
        val question = TriviaQuestionGenerator.generate("He", allSymbols, fact, Random(42))
        requireNotNull(question)
        assertEquals(4, question.choices.size)
        assertEquals(4, question.choices.toSet().size)
        assertTrue(question.choices.contains("He"))
        assertEquals("He", question.correctSymbol)
        assertEquals("A noble gas.", question.fact)
    }

    @Test
    fun generate_repeatedCalls_neverCrash_andAlwaysProduceValidQuestion() {
        repeat(200) { seed ->
            val question = TriviaQuestionGenerator.generate("He", allSymbols, fact, Random(seed.toLong()))
            requireNotNull(question)
            assertEquals(4, question.choices.toSet().size)
            assertTrue(question.choices.contains("He"))
        }
    }

    @Test
    fun missingFactForCorrectSymbol_returnsNull() {
        val question = TriviaQuestionGenerator.generate("H", allSymbols, fact, Random(1))
        assertNull(question)
    }

    @Test
    fun blankFact_treatedAsMissing_returnsNull() {
        val blankFact: (String) -> String? = { "" }
        val question = TriviaQuestionGenerator.generate("He", allSymbols, blankFact, Random(1))
        assertNull(question)
    }

    @Test
    fun fewerThanFourSymbolsAvailable_returnsNull() {
        val tooFewSymbols = listOf("H", "He") // only 1 possible distractor, need 3
        val question = TriviaQuestionGenerator.generate("He", tooFewSymbols, fact, Random(1))
        assertNull(question)
    }

    @Test
    fun exactlyFourSymbolsAvailable_stillProducesAValidQuestion() {
        val exactlyFour = listOf("H", "He", "Li", "Be")
        val question = TriviaQuestionGenerator.generate("He", exactlyFour, fact, Random(1))
        requireNotNull(question)
        assertEquals(exactlyFour.toSet(), question.choices.toSet())
    }
}
