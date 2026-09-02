package com.mckimquyen.atomicPeriodicTable.feature.exam

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Exercises the exact Gson serialize/deserialize shape ExamHistoryPref relies on, without an
 * Android Context (SharedPreferences itself is covered by the instrumented ExamHistoryPrefTest).
 */
class ExamResultSerializationTest {

    private val gson = Gson()
    private val historyType = object : TypeToken<List<ExamResult>>() {}.type

    @Test
    fun roundTrip_singleResult_preservesAllFields() {
        val original = listOf(ExamResult(score = 18, total = 20, timestampMs = 1_700_000_000_000L))
        val json = gson.toJson(original)
        val decoded: List<ExamResult> = gson.fromJson(json, historyType)
        assertEquals(original, decoded)
    }

    @Test
    fun roundTrip_multipleResults_preservesOrder() {
        val original = listOf(
            ExamResult(20, 20, 3L),
            ExamResult(15, 20, 2L),
            ExamResult(10, 20, 1L),
        )
        val json = gson.toJson(original)
        val decoded: List<ExamResult> = gson.fromJson(json, historyType)
        assertEquals(original, decoded)
    }

    @Test
    fun roundTrip_emptyList() {
        val json = gson.toJson(emptyList<ExamResult>())
        val decoded: List<ExamResult> = gson.fromJson(json, historyType)
        assertEquals(emptyList<ExamResult>(), decoded)
    }
}
