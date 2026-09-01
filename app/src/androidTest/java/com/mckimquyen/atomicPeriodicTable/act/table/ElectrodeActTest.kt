package com.mckimquyen.atomicPeriodicTable.act.table

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for FIX-021: rows had the ripple foreground + isClickable/isFocusable=true
 * (visually tappable) but no setOnClickListener was ever attached — tapping did nothing.
 * item.name matches a real periodic element, so tapping a row now opens its detail screen.
 */
@RunWith(AndroidJUnit4::class)
class ElectrodeActTest {

    @Test
    fun tappingFirstRow_setsElementSendAndLoadToThatRowsElement() {
        ActivityScenario.launch(ElectrodeAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.eView)
                recyclerView.scrollToPosition(0)
                recyclerView.layoutManager?.findViewByPosition(0)?.performClick()
            }

            val actual = ElementSendAndLoad(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            ).getValue()
            // SeriesModel's first entry is "lithium" — see model/SeriesModel.kt.
            assertEquals("tapping the first row must load that row's element", "lithium", actual)
        }
    }
}
