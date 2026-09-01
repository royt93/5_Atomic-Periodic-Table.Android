package com.mckimquyen.atomicPeriodicTable.act.table

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.model.Ion
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Method

/**
 * Regression guard for FIX-026: filter() used to create a brand new Handler on every call
 * instead of reusing one and cancelling the previous pending callback. Reassigning the field
 * to a new Handler instance doesn't cancel a prior instance's already-posted delayed
 * callback, so fast typing left several stale callbacks racing to update the empty-state view.
 * Same fix applied identically in ElectrodeAct, EquationsAct, IsotopesActExperimental.
 */
@RunWith(AndroidJUnit4::class)
class IonActTest {

    @Test
    fun filterHandler_isReusedAcrossCalls_insteadOfRecreated() {
        val filterMethod: Method = IonAct::class.java.getDeclaredMethod(
            "filter",
            String::class.java,
            ArrayList::class.java,
            RecyclerView::class.java,
        ).apply { isAccessible = true }
        val handlerField = IonAct::class.java.getDeclaredField("filterHandler").apply { isAccessible = true }

        var firstHandler: Any? = null
        var secondHandler: Any? = null

        ActivityScenario.launch(IonAct::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.ionView)
                filterMethod.invoke(activity, "a", ArrayList<Ion>(), recyclerView)
                firstHandler = handlerField.get(activity)

                filterMethod.invoke(activity, "ab", ArrayList<Ion>(), recyclerView)
                secondHandler = handlerField.get(activity)
            }
        }

        assertSame(
            "filter() must reuse the same Handler instance across calls, not create a new one each time",
            firstHandler,
            secondHandler,
        )
    }
}
