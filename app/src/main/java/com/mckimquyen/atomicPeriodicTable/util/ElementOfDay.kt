package com.mckimquyen.atomicPeriodicTable.util

/**
 * Pure/Android-independent so the daily pick is JVM-unit-testable without a Context.
 * epochDay is days-since-epoch (e.g. System.currentTimeMillis() / 86_400_000L) rather than
 * java.time.LocalDate, since minSdk 24 predates LocalDate (API 26) without desugaring.
 */
object ElementOfDay {
    fun indexForDay(epochDay: Long, totalElements: Int): Int = (epochDay % totalElements).toInt()
}
