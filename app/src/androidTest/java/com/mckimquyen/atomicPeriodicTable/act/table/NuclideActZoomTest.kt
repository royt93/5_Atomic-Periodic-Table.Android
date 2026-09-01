package com.mckimquyen.atomicPeriodicTable.act.table

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for FIX-032/FIX-033: dispatchTouchEvent() fed the same MotionEvent into
 * mScaleDetector.onTouchEvent() twice, and onScale() applied `mScale += scale` twice, then
 * both clamp branches forced mScale back to exactly 1f on ANY deviation — together this made
 * pinch-zoom a complete no-op (mScale always ended up exactly 1f after any gesture).
 */
@RunWith(AndroidJUnit4::class)
class NuclideActZoomTest {

    @Test
    fun pinchOutGesture_actuallyChangesScale_insteadOfSnappingBackToExactlyOne() {
        ActivityScenario.launch(NuclideAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val downTime = SystemClock.uptimeMillis()

                dispatch(activity, twoFingerEvent(downTime, MotionEvent.ACTION_DOWN, downTime, 400f, 500f, 400f, 500f, pointerCount = 1))
                dispatch(activity, twoFingerEvent(downTime, actionPointerDown(1), downTime, 400f, 500f, 420f, 500f, pointerCount = 2))
                // Spread the two pointers apart — a zoom-in (pinch-out) gesture.
                for (step in 1..10) {
                    val t = downTime + step * 16L
                    val spread = 20f + step * 40f
                    dispatch(activity, twoFingerEvent(downTime, MotionEvent.ACTION_MOVE, t, 400f - spread, 500f, 400f + spread, 500f, pointerCount = 2))
                }
                dispatch(activity, twoFingerEvent(downTime, actionPointerUp(1), downTime, 400f, 500f, 420f, 500f, pointerCount = 2))
                dispatch(activity, twoFingerEvent(downTime, MotionEvent.ACTION_UP, downTime, 400f, 500f, 400f, 500f, pointerCount = 1))

                assertNotEquals(
                    "a real pinch-out gesture must move mScale away from 1f, not snap back to exactly 1f every time",
                    1f,
                    activity.mScale,
                )
                assertTrue("mScale must stay within its clamped bounds", activity.mScale in 0.4f..1f)
            }
        }
    }

    private fun dispatch(activity: NuclideAct, event: MotionEvent) {
        activity.dispatchTouchEvent(event)
        event.recycle()
    }

    private fun actionPointerDown(pointerIndex: Int) =
        MotionEvent.ACTION_POINTER_DOWN or (pointerIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)

    private fun actionPointerUp(pointerIndex: Int) =
        MotionEvent.ACTION_POINTER_UP or (pointerIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)

    private fun twoFingerEvent(
        downTime: Long,
        action: Int,
        eventTime: Long,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        pointerCount: Int,
    ): MotionEvent {
        val properties = Array(pointerCount) { i ->
            MotionEvent.PointerProperties().apply {
                id = i
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        }
        val coords = arrayOf(
            MotionEvent.PointerCoords().apply { x = x0; y = y0; pressure = 1f; size = 1f },
            MotionEvent.PointerCoords().apply { x = x1; y = y1; pressure = 1f; size = 1f },
        ).copyOfRange(0, pointerCount)

        return MotionEvent.obtain(
            downTime,
            eventTime,
            action,
            pointerCount,
            properties,
            coords,
            0,
            0,
            1f,
            1f,
            0,
            0,
            0,
            0,
        )
    }
}
