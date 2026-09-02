package com.mckimquyen.atomicPeriodicTable.feature.filter

import com.mckimquyen.atomicPeriodicTable.model.Element
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementFilterTest {

    private val hydrogen = Element("hydrogen", "H", 1, 2.20, 7)
    private val helium = Element("helium", "He", 2, 0.0, 8)
    private val lithium = Element("lithium", "Li", 3, 0.98, 11)

    private val mass: (String) -> Double? = { symbol ->
        when (symbol) {
            "H" -> 1.008
            "He" -> 4.0026
            "Li" -> 6.94
            else -> null
        }
    }

    private val category: (String) -> String? = { symbol ->
        when (symbol) {
            "H" -> "Other Nonmetals"
            "He" -> "Noble Gases"
            "Li" -> "Alkali Metals"
            else -> null
        }
    }

    @Test
    fun emptyCriteria_matchesEverything() {
        assertTrue(ElementFilter.matches(hydrogen, FilterCriteria(), mass, category))
        assertTrue(ElementFilter.matches(lithium, FilterCriteria(), mass, category))
    }

    @Test
    fun massRange_includesElementsInsideRange() {
        val criteria = FilterCriteria(minMass = 4.0, maxMass = 7.0)
        assertFalse(ElementFilter.matches(hydrogen, criteria, mass, category))
        assertTrue(ElementFilter.matches(helium, criteria, mass, category))
        assertTrue(ElementFilter.matches(lithium, criteria, mass, category))
    }

    @Test
    fun massRange_excludesElementsOutsideRange() {
        val criteria = FilterCriteria(minMass = 100.0)
        assertFalse(ElementFilter.matches(hydrogen, criteria, mass, category))
        assertFalse(ElementFilter.matches(lithium, criteria, mass, category))
    }

    @Test
    fun massRange_missingMassData_excludesElement() {
        val noMass: (String) -> Double? = { null }
        val criteria = FilterCriteria(minMass = 0.0)
        assertFalse(ElementFilter.matches(hydrogen, criteria, noMass, category))
    }

    @Test
    fun electronegativityRange_filtersOnElementFieldDirectly() {
        val criteria = FilterCriteria(minElectronegativity = 1.0)
        assertTrue(ElementFilter.matches(hydrogen, criteria, mass, category)) // 2.20
        assertFalse(ElementFilter.matches(helium, criteria, mass, category)) // 0.0
        assertFalse(ElementFilter.matches(lithium, criteria, mass, category)) // 0.98
    }

    @Test
    fun electronegativityRange_upperBound_excludesElementsAboveMax() {
        val criteria = FilterCriteria(maxElectronegativity = 1.5)
        assertFalse(ElementFilter.matches(hydrogen, criteria, mass, category)) // 2.20 > 1.5
        assertTrue(ElementFilter.matches(helium, criteria, mass, category)) // 0.0
        assertTrue(ElementFilter.matches(lithium, criteria, mass, category)) // 0.98
    }

    @Test
    fun categoryFilter_matchesOnlyThatCategory() {
        val criteria = FilterCriteria(category = "Noble Gases")
        assertFalse(ElementFilter.matches(hydrogen, criteria, mass, category))
        assertTrue(ElementFilter.matches(helium, criteria, mass, category))
        assertFalse(ElementFilter.matches(lithium, criteria, mass, category))
    }

    @Test
    fun combinedCriteria_allConditionsMustMatch() {
        val criteria = FilterCriteria(minMass = 1.0, maxMass = 10.0, category = "Alkali Metals")
        assertFalse(ElementFilter.matches(hydrogen, criteria, mass, category)) // wrong category
        assertTrue(ElementFilter.matches(lithium, criteria, mass, category)) // mass 6.94 + right category
    }

    @Test
    fun isEmpty_trueOnlyWhenNoConditionsSet() {
        assertTrue(FilterCriteria().isEmpty)
        assertFalse(FilterCriteria(minMass = 1.0).isEmpty)
        assertFalse(FilterCriteria(category = "Halogens").isEmpty)
    }
}
