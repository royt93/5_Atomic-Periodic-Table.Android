package com.mckimquyen.atomicPeriodicTable.act

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompareActTest {

    @Test
    fun launching_showsDefaultTwoElementComparison_withThirdColumnHidden() {
        ActivityScenario.launch(CompareAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertSame(View.GONE, activity.findViewById<View>(R.id.cardElement3).visibility)
                assertSame(View.GONE, activity.findViewById<View>(R.id.compareVsSpacer2).visibility)
                assertSame(View.GONE, activity.findViewById<View>(R.id.tvHeaderElement3).visibility)

                val tableBody = activity.findViewById<android.view.ViewGroup>(R.id.compareTableBody)
                assertTrue("expected at least one comparison row", tableBody.childCount > 0)

                // Regression guard: every row's 3rd-column cell stays GONE when the 3rd
                // element isn't active — the row layout defaults to GONE, this proves
                // bindCompareRow() never flips it on in the 2-element path.
                for (i in 0 until tableBody.childCount) {
                    val val3 = tableBody.getChildAt(i).findViewById<TextView>(R.id.tvCompareVal3)
                    assertSame("row $i", View.GONE, val3.visibility)
                }
            }
        }
    }

    @Test
    fun tappingToggleButton_revealsThirdColumn_andTappingAgainHidesIt() {
        ActivityScenario.launch(CompareAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.compareToggleThirdBtn).performClick()

                assertSame(View.VISIBLE, activity.findViewById<View>(R.id.cardElement3).visibility)
                assertSame(View.VISIBLE, activity.findViewById<View>(R.id.compareVsSpacer2).visibility)
                assertSame(View.VISIBLE, activity.findViewById<View>(R.id.tvHeaderElement3).visibility)
                assertEquals(
                    activity.getString(R.string.compare_remove_third_element),
                    (activity.findViewById<TextView>(R.id.compareToggleThirdBtn)).text.toString()
                )

                val tableBody = activity.findViewById<android.view.ViewGroup>(R.id.compareTableBody)
                val firstRowVal3 = tableBody.getChildAt(0).findViewById<TextView>(R.id.tvCompareVal3)
                assertSame(View.VISIBLE, firstRowVal3.visibility)

                activity.findViewById<View>(R.id.compareToggleThirdBtn).performClick()

                assertSame(View.GONE, activity.findViewById<View>(R.id.cardElement3).visibility)
                assertEquals(
                    activity.getString(R.string.compare_add_third_element),
                    (activity.findViewById<TextView>(R.id.compareToggleThirdBtn)).text.toString()
                )
            }
        }
    }

    @Test
    fun thirdElementActive_atomicNumberRow_ranksAllThreeColumnsCorrectly() {
        // Default trio once the 3rd column is toggled on: H (1), He (2), Li (3) — atomic number
        // is a clean, always-numeric field to prove the 3-way ranking end-to-end.
        ActivityScenario.launch(CompareAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.compareToggleThirdBtn).performClick()

                val tableBody = activity.findViewById<android.view.ViewGroup>(R.id.compareTableBody)
                val atomicNumberRow = tableBody.getChildAt(0) // "Atomic Number" is bound first
                val val1 = atomicNumberRow.findViewById<TextView>(R.id.tvCompareVal1)
                val val2 = atomicNumberRow.findViewById<TextView>(R.id.tvCompareVal2)
                val val3 = atomicNumberRow.findViewById<TextView>(R.id.tvCompareVal3)

                assertEquals("1", val1.text.toString())
                assertEquals("2", val2.text.toString())
                assertEquals("3", val3.text.toString())

                val colorHigh = ContextCompat.getColor(activity, R.color.compare_higher)
                val colorLow = ContextCompat.getColor(activity, R.color.compare_lower)

                assertEquals("H (lowest) must be LOW-colored", colorLow, val1.currentTextColor)
                assertEquals("Li (highest) must be HIGH-colored", colorHigh, val3.currentTextColor)
                // He is the middle value — must be neither the high nor the low color.
                assertTrue(val2.currentTextColor != colorHigh && val2.currentTextColor != colorLow)
            }
        }
    }
}
