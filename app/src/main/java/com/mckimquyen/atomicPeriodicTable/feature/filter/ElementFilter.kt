package com.mckimquyen.atomicPeriodicTable.feature.filter

import com.mckimquyen.atomicPeriodicTable.model.Element

/** Null bound = no restriction on that side of the range. */
data class FilterCriteria(
    val minMass: Double? = null,
    val maxMass: Double? = null,
    val minElectronegativity: Double? = null,
    val maxElectronegativity: Double? = null,
    val category: String? = null,
) {
    val isEmpty: Boolean
        get() = minMass == null && maxMass == null &&
            minElectronegativity == null && maxElectronegativity == null &&
            category == null
}

/** Pure/Android-independent so the matching logic is JVM-testable without ElementWeightCache/Context. */
object ElementFilter {
    fun matches(
        element: Element,
        criteria: FilterCriteria,
        massLookup: (String) -> Double?,
        categoryLookup: (String) -> String?,
    ): Boolean {
        if (criteria.minMass != null || criteria.maxMass != null) {
            val mass = massLookup(element.short) ?: return false
            if (criteria.minMass != null && mass < criteria.minMass) return false
            if (criteria.maxMass != null && mass > criteria.maxMass) return false
        }
        if (criteria.minElectronegativity != null && element.electro < criteria.minElectronegativity) return false
        if (criteria.maxElectronegativity != null && element.electro > criteria.maxElectronegativity) return false
        if (criteria.category != null && categoryLookup(element.short) != criteria.category) return false
        return true
    }
}
