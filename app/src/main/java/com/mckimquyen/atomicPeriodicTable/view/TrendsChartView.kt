package com.mckimquyen.atomicPeriodicTable.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.color.MaterialColors
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.feature.trends.TrendsMapper

/**
 * Simple scatter/line chart of one numeric Element property (currently electronegativity)
 * across atomic number. Deliberately hand-drawn on Canvas (matches ConfettiView's pattern) —
 * no chart library dependency for a single line+dot plot.
 */
class TrendsChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = context.resources.displayMetrics.density
    private val paddingPx = 24f * density
    private val dotRadiusPx = 4f * density
    private val selectedDotRadiusPx = 8f * density
    private val tapSlopPx = 24f * density

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = MaterialColors.getColor(this@TrendsChartView, R.attr.colorPrimary)
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = MaterialColors.getColor(this@TrendsChartView, R.attr.colorPrimary)
    }
    private val selectedDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = MaterialColors.getColor(this@TrendsChartView, R.attr.colorTertiary)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * density
        color = MaterialColors.getColor(this@TrendsChartView, R.attr.colorOnSurfaceVariant)
    }

    private var elements: List<Element> = emptyList()
    private var selectedIndex: Int? = null
    private var onPointSelected: ((Element) -> Unit)? = null

    private var cachedPoints: List<Pair<Element, Pair<Float, Float>>> = emptyList()

    fun setElements(list: List<Element>) {
        elements = list
        selectedIndex = null
        invalidate()
    }

    fun setOnPointSelectedListener(listener: (Element) -> Unit) {
        onPointSelected = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val validElements = elements.filter { it.electro > 0.0 }
        if (validElements.isEmpty()) return

        val minNumber = elements.minOf { it.number }
        val maxNumber = elements.maxOf { it.number }
        val minValue = validElements.minOf { it.electro }
        val maxValue = validElements.maxOf { it.electro }

        val points = validElements.map { element ->
            val x = TrendsMapper.mapX(element.number, minNumber, maxNumber, width.toFloat(), paddingPx)
            val y = TrendsMapper.mapY(element.electro, minValue, maxValue, height.toFloat(), paddingPx)
            element to (x to y)
        }
        cachedPoints = points

        val path = Path()
        points.forEachIndexed { index, (_, xy) ->
            if (index == 0) path.moveTo(xy.first, xy.second) else path.lineTo(xy.first, xy.second)
        }
        canvas.drawPath(path, linePaint)

        points.forEachIndexed { index, (_, xy) ->
            val paint = if (index == selectedIndex) selectedDotPaint else dotPaint
            val radius = if (index == selectedIndex) selectedDotRadiusPx else dotRadiusPx
            canvas.drawCircle(xy.first, xy.second, radius, paint)
        }

        canvas.drawText(maxValue.toString(), paddingPx, paddingPx, labelPaint)
        canvas.drawText(minValue.toString(), paddingPx, height - paddingPx / 2, labelPaint)
        canvas.drawText(minNumber.toString(), paddingPx, height - paddingPx / 2 + labelPaint.textSize, labelPaint)
        canvas.drawText(
            maxNumber.toString(),
            width - paddingPx - labelPaint.measureText(maxNumber.toString()),
            height - paddingPx / 2 + labelPaint.textSize,
            labelPaint,
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)
        val index = TrendsMapper.nearestPointIndex(event.x, event.y, cachedPoints.map { it.second }) ?: return false
        val (element, xy) = cachedPoints[index]
        val distance = kotlin.math.hypot(xy.first - event.x, xy.second - event.y)
        if (distance > tapSlopPx) return false
        selectedIndex = index
        invalidate()
        onPointSelected?.invoke(element)
        return true
    }
}
