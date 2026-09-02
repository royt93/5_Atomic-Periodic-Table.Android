package com.mckimquyen.atomicPeriodicTable.feature.quiz

import com.mckimquyen.atomicPeriodicTable.model.Element
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFilterTest {

    private val elements = listOf(
        Element("hydrogen", "H", 1, 2.20, 7),
        Element("helium", "He", 2, 0.0, 8),
        Element("lithium", "Li", 3, 0.98, 11),
        Element("neon", "Ne", 10, 0.0, 20),
    )

    private val lookup: (String) -> String? = { symbol ->
        when (symbol) {
            "H" -> "Other Nonmetals"
            "He" -> "Noble Gases"
            "Ne" -> "Noble Gases"
            "Li" -> "Alkali Metals"
            else -> null
        }
    }

    @Test
    fun matchingCategory_returnsOnlyThoseElements() {
        val result = CategoryFilter.filterElementsByCategory(elements, "Noble Gases", lookup)
        assertEquals(listOf("He", "Ne"), result.map { it.short })
    }

    @Test
    fun categoryWithNoMatches_returnsEmptyList() {
        val result = CategoryFilter.filterElementsByCategory(elements, "Halogens", lookup)
        assertTrue(result.isEmpty())
    }

    @Test
    fun lookupReturningNull_excludesThatElement() {
        val lookupWithGap: (String) -> String? = { symbol -> if (symbol == "H") null else lookup(symbol) }
        val result = CategoryFilter.filterElementsByCategory(elements, "Other Nonmetals", lookupWithGap)
        assertTrue(result.isEmpty())
    }

    @Test
    fun allCategories_hasTenUniqueEntries() {
        assertEquals(10, CategoryFilter.ALL_CATEGORIES.size)
        assertEquals(10, CategoryFilter.ALL_CATEGORIES.toSet().size)
    }
}
