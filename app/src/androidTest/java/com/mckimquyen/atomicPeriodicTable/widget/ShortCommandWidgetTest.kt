package com.mckimquyen.atomicPeriodicTable.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.util.ElementOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortCommandWidgetTest {

    @Test
    fun testWidgetProviderInstantiation() {
        val widgetProvider = ShortCommandWidget()
        assertNotNull("Widget provider should not be null", widgetProvider)
    }

    @Test
    fun testWidgetOnUpdateDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widgetProvider = ShortCommandWidget()
        
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = intArrayOf(1, 2)
        
        // Ensure calling onUpdate does not throw any exceptions
        widgetProvider.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    // Regression guard for the Element-of-the-Day widget: onUpdate() must point
    // ElementSendAndLoad (read by ElementInfoAct on launch) at the same element
    // ElementOfDay.indexForDay() picks for today, so tapping the widget opens the
    // element it's actually displaying, not whatever was last viewed in the app.
    @Test
    fun onUpdate_pointsElementSendAndLoad_atTodaysElement() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widgetProvider = ShortCommandWidget()
        val appWidgetManager = AppWidgetManager.getInstance(context)

        widgetProvider.onUpdate(context, appWidgetManager, intArrayOf(1))

        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val expected = elements[ElementOfDay.indexForDay(System.currentTimeMillis() / 86_400_000L, elements.size)]

        assertEquals(expected.element, ElementSendAndLoad(context).getValue())
    }
}
