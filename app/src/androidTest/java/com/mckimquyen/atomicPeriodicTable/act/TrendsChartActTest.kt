package com.mckimquyen.atomicPeriodicTable.act

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.trends.TrendsMapper
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.view.TrendsChartView
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrendsChartActTest {

    @Test
    fun launch_showsChartCardAndTapHint() {
        ActivityScenario.launch(TrendsChartAct::class.java).use {
            onView(withId(R.id.cardTrendsChart)).check(matches(isDisplayed()))
            onView(withId(R.id.tvTrendsTooltip)).check(matches(withText(R.string.trends_tap_hint)))
        }
    }

    @Test
    fun tapKnownDataPoint_updatesTooltipWithThatElementsSymbol() {
        ActivityScenario.launch(TrendsChartAct::class.java).use { scenario ->
            var targetX = 0f
            var targetY = 0f
            var expectedSymbol = ""

            scenario.onActivity { activity ->
                val chartView = activity.findViewById<TrendsChartView>(R.id.trendsChartView)
                val elements = ArrayList<Element>()
                ElementModel.getList(elements)
                val validElements = elements.filter { it.electro > 0.0 }
                val minNumber = elements.minOf { it.number }
                val maxNumber = elements.maxOf { it.number }
                val minValue = validElements.minOf { it.electro }
                val maxValue = validElements.maxOf { it.electro }
                val target = validElements[validElements.size / 2]
                expectedSymbol = target.short

                val paddingPx = 24f * chartView.resources.displayMetrics.density
                targetX = TrendsMapper.mapX(target.number, minNumber, maxNumber, chartView.width.toFloat(), paddingPx)
                targetY = TrendsMapper.mapY(target.electro, minValue, maxValue, chartView.height.toFloat(), paddingPx)

                val downTime = SystemClock.uptimeMillis()
                val event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, targetX, targetY, 0)
                chartView.dispatchTouchEvent(event)
                event.recycle()
            }

            onView(withId(R.id.tvTrendsTooltip)).check(matches(withText(org.hamcrest.Matchers.containsString(expectedSymbol))))
        }
    }

    @Test
    fun tapFarOutsideAnyPoint_leavesTooltipUnchanged() {
        ActivityScenario.launch(TrendsChartAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chartView = activity.findViewById<TrendsChartView>(R.id.trendsChartView)
                // (0, 0) is the chart's top-left corner, outside the tap-slop radius of the
                // nearest plotted point (padding keeps every point away from the raw edges).
                val downTime = SystemClock.uptimeMillis()
                val event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
                chartView.dispatchTouchEvent(event)
                event.recycle()
            }
            onView(withId(R.id.tvTrendsTooltip)).check(matches(withText(R.string.trends_tap_hint)))
        }
    }

    @Test
    fun tappingTwoDifferentPoints_updatesTooltipEachTime() {
        ActivityScenario.launch(TrendsChartAct::class.java).use { scenario ->
            val symbols = mutableListOf<String>()

            scenario.onActivity { activity ->
                val chartView = activity.findViewById<TrendsChartView>(R.id.trendsChartView)
                val elements = ArrayList<Element>()
                ElementModel.getList(elements)
                val validElements = elements.filter { it.electro > 0.0 }
                val minNumber = elements.minOf { it.number }
                val maxNumber = elements.maxOf { it.number }
                val minValue = validElements.minOf { it.electro }
                val maxValue = validElements.maxOf { it.electro }
                val paddingPx = 24f * chartView.resources.displayMetrics.density

                listOf(validElements.first(), validElements.last()).forEach { target ->
                    symbols += target.short
                    val x = TrendsMapper.mapX(target.number, minNumber, maxNumber, chartView.width.toFloat(), paddingPx)
                    val y = TrendsMapper.mapY(target.electro, minValue, maxValue, chartView.height.toFloat(), paddingPx)
                    val downTime = SystemClock.uptimeMillis()
                    val event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
                    chartView.dispatchTouchEvent(event)
                    event.recycle()
                }
            }

            assertTrue("expected 2 distinct symbols to compare, got $symbols", symbols[0] != symbols[1])
            onView(withId(R.id.tvTrendsTooltip)).check(matches(withText(org.hamcrest.Matchers.containsString(symbols.last()))))
        }
    }
}
