package com.mckimquyen.atomicPeriodicTable.feature.share

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.act.ElementInfoAct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// Uses a real themed Activity as the render Context (not ApplicationProvider's bare
// application context) — the layout references ?attr/colorPrimaryContainer etc., which only
// resolve against a context that actually has AppTheme applied, same as real usage from
// ElementInfoAct.shareCurrentElement().
@RunWith(AndroidJUnit4::class)
class ElementCardRendererTest {

    @Test
    fun render_producesSquareBitmapAtExpectedSize() {
        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val bitmap = ElementCardRenderer.render(
                    context = activity,
                    symbol = "Au",
                    name = "Gold",
                    number = 79,
                    massText = "196.97 u",
                    categoryText = "Transition Metals",
                )
                val expectedSizePx = (360 * activity.resources.displayMetrics.density).toInt()
                assertEquals(expectedSizePx, bitmap.width)
                assertEquals(expectedSizePx, bitmap.height)
            }
        }
    }

    @Test
    fun render_actuallyDrawsContent_notAUniformBlankBitmap() {
        ActivityScenario.launch(ElementInfoAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val bitmap = ElementCardRenderer.render(
                    context = activity,
                    symbol = "H",
                    name = "Hydrogen",
                    number = 1,
                    massText = "1.01 u",
                    categoryText = "Nonmetal",
                )
                val colors = (0 until bitmap.height step 10).map { y -> bitmap.getPixel(bitmap.width / 2, y) }.toSet()
                assertTrue("expected multiple distinct colors (text drawn over background), got $colors", colors.size > 1)
            }
        }
    }
}
