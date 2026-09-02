package com.mckimquyen.atomicPeriodicTable.act

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.feature.compound.CompoundMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompoundBuilderActTest {

    private fun tapChip(chipGroup: ChipGroup, symbol: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text == symbol) {
                chip.performClick()
                return
            }
        }
        throw AssertionError("no chip found for symbol $symbol")
    }

    @Test
    fun launch_showsOneChipPerAvailableSymbol_andNoResultCard() {
        ActivityScenario.launch(CompoundBuilderAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupElements)
                assertEquals(CompoundMatcher.availableSymbols.size, chipGroup.childCount)

                val resultCard = activity.findViewById<android.view.View>(R.id.cardResult)
                assertEquals(android.view.View.GONE, resultCard.visibility)
            }
        }
    }

    @Test
    fun tappingHTwiceThenO_showsWaterFound() {
        ActivityScenario.launch(CompoundBuilderAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupElements)
                tapChip(chipGroup, "H")
                tapChip(chipGroup, "H")
                tapChip(chipGroup, "O")

                val resultCard = activity.findViewById<android.view.View>(R.id.cardResult)
                assertEquals(android.view.View.VISIBLE, resultCard.visibility)

                val resultText = activity.findViewById<android.widget.TextView>(R.id.tvResult)
                assertTrue(resultText.text.contains("H2O"))
            }
        }
    }

    @Test
    fun tappingSingleH_showsNotFound_becauseWaterNeedsTwo() {
        ActivityScenario.launch(CompoundBuilderAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupElements)
                tapChip(chipGroup, "H")

                val resultCard = activity.findViewById<android.view.View>(R.id.cardResult)
                assertEquals(android.view.View.VISIBLE, resultCard.visibility)

                val resultText = activity.findViewById<android.widget.TextView>(R.id.tvResult)
                assertEquals(activity.getString(R.string.compound_builder_not_found), resultText.text)
            }
        }
    }

    @Test
    fun clearButton_resetsSelectionAndHidesResultCard() {
        ActivityScenario.launch(CompoundBuilderAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val chipGroup = activity.findViewById<ChipGroup>(R.id.chipGroupElements)
                tapChip(chipGroup, "H")
                tapChip(chipGroup, "H")
                tapChip(chipGroup, "O")

                activity.findViewById<android.view.View>(R.id.btnClearSelection).performClick()

                val resultCard = activity.findViewById<android.view.View>(R.id.cardResult)
                assertEquals(android.view.View.GONE, resultCard.visibility)

                val preview = activity.findViewById<android.widget.TextView>(R.id.tvSelectionPreview)
                assertEquals(activity.getString(R.string.compound_builder_empty_selection), preview.text)
            }
        }
    }

    @Test
    fun backButton_finishesActivity() {
        ActivityScenario.launch(CompoundBuilderAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.compoundBackBtn).performClick()
                assertTrue(activity.isFinishing)
            }
        }
    }
}
