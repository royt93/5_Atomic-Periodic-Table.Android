package com.mckimquyen.atomicPeriodicTable.feature.quiz

import com.mckimquyen.atomicPeriodicTable.model.Element

/** Pure/Android-independent so the filter logic is JVM-testable without ElementWeightCache/Context. */
object CategoryFilter {

    /** Raw (untranslated) category names, matching ElementWeightCache's `element_group` JSON values. */
    val ALL_CATEGORIES = listOf(
        "Other Nonmetals", "Noble Gases", "Alkali Metals", "Alkaline Earth Metals",
        "Transition Metals", "Lanthanides", "Actinides", "Post-transition Metals", "Metalloids", "Halogens"
    )

    fun filterElementsByCategory(
        elements: List<Element>,
        category: String,
        categoryLookup: (String) -> String?,
    ): List<Element> = elements.filter { categoryLookup(it.short) == category }
}
