package com.mckimquyen.atomicPeriodicTable.setting

import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExperimentalActTest {

    // Regression guard for FIX-022: onApplySystemInsets used to do `params.height += top`
    // on every call instead of `= base + top`. Since this callback can fire more than once
    // with the same absolute inset (rotation, keyboard, system bar visibility change), the
    // title bar/header grew a little more every time instead of staying stable.
    @Test
    fun onApplySystemInsets_calledTwiceWithSameInset_doesNotGrowCumulatively() {
        ActivityScenario.launch(ExperimentalAct::class.java).use { scenario ->
            var heightAfterFirst = 0
            var marginAfterFirst = 0
            var heightAfterSecond = 0
            var marginAfterSecond = 0

            scenario.onActivity { activity ->
                val titleBar = activity.findViewById<View>(R.id.commonTitleBackExp)
                val header = activity.findViewById<View>(R.id.generalHeaderExp)

                activity.onApplySystemInsets(top = 50, bottom = 0, left = 0, right = 0)
                heightAfterFirst = titleBar.layoutParams.height
                marginAfterFirst = (header.layoutParams as ViewGroup.MarginLayoutParams).topMargin

                activity.onApplySystemInsets(top = 50, bottom = 0, left = 0, right = 0)
                heightAfterSecond = titleBar.layoutParams.height
                marginAfterSecond = (header.layoutParams as ViewGroup.MarginLayoutParams).topMargin
            }

            assertEquals(
                "calling onApplySystemInsets again with the same top must not grow the title bar height",
                heightAfterFirst,
                heightAfterSecond,
            )
            assertEquals(
                "calling onApplySystemInsets again with the same top must not grow the header top margin",
                marginAfterFirst,
                marginAfterSecond,
            )
        }
    }
}
