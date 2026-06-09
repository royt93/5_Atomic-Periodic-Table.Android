package com.mckimquyen.atomicPeriodicTable.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
