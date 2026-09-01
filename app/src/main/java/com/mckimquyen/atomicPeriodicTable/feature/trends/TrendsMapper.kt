package com.mckimquyen.atomicPeriodicTable.feature.trends

/**
 * Pure/Android-independent data<->pixel mapping for TrendsChartView, so the chart math is
 * JVM-unit-testable without a Canvas/View. Same convention as VipCalculator/FlashcardScheduler.
 */
object TrendsMapper {

    fun mapX(atomicNumber: Int, minNumber: Int, maxNumber: Int, widthPx: Float, paddingPx: Float): Float {
        if (maxNumber == minNumber) return paddingPx
        val ratio = (atomicNumber - minNumber).toFloat() / (maxNumber - minNumber)
        return paddingPx + ratio * (widthPx - 2 * paddingPx)
    }

    /** Inverted: higher value draws closer to the top (smaller y), matching normal chart reading direction. */
    fun mapY(value: Double, minValue: Double, maxValue: Double, heightPx: Float, paddingPx: Float): Float {
        if (maxValue == minValue) return heightPx - paddingPx
        val ratio = ((value - minValue) / (maxValue - minValue)).toFloat()
        return heightPx - paddingPx - ratio * (heightPx - 2 * paddingPx)
    }

    /** Index of the closest point to (touchX, touchY), or null if [pointsPx] is empty. */
    fun nearestPointIndex(touchX: Float, touchY: Float, pointsPx: List<Pair<Float, Float>>): Int? {
        var bestIndex: Int? = null
        var bestDistanceSq = Float.MAX_VALUE
        pointsPx.forEachIndexed { index, (px, py) ->
            val dx = px - touchX
            val dy = py - touchY
            val distanceSq = dx * dx + dy * dy
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq
                bestIndex = index
            }
        }
        return bestIndex
    }
}
