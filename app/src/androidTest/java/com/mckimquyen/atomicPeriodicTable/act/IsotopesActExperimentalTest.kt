package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.model.Element
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for FIX-025: ElementModel.getList() always appends (never clears), so
 * drawCard(elementList) growing the same persistent field on every click made it accumulate
 * 118 -> 236 -> 354 duplicate entries instead of staying at 118.
 */
@RunWith(AndroidJUnit4::class)
class IsotopesActExperimentalTest {

    @Test
    fun elementListStaysAt118_afterMultipleElementClicks() {
        ActivityScenario.launch(IsotopesActExperimental::class.java).use { scenario ->
            val item = Element("hydrogen", "H", 1, 2.20, 7)

            scenario.onActivity { activity ->
                activity.elementClickListener(item, 0)
                activity.elementClickListener(item, 0)
                activity.elementClickListener(item, 0)
            }

            scenario.onActivity { activity ->
                val field = IsotopesActExperimental::class.java.getDeclaredField("elementList")
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val list = field.get(activity) as ArrayList<Element>
                assertEquals(
                    "elementList must not accumulate duplicates across repeated drawCard() calls",
                    118,
                    list.size,
                )
            }
        }
    }
}
