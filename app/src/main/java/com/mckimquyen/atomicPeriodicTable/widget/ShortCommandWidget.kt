package com.mckimquyen.atomicPeriodicTable.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.ElementInfoAct
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.util.ElementOfDay
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache

/**
 * Implementation of App Widget functionality.
 * Shows an "Element of the Day" picked deterministically by day (see ElementOfDay),
 * refreshed by the platform every 24h via android:updatePeriodMillis (no WorkManager needed).
 */
class ShortCommandWidget : AppWidgetProvider() {
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val today = elements[ElementOfDay.indexForDay(System.currentTimeMillis() / 86_400_000L, elements.size)]
        ElementWeightCache.init(context)
        ElementSendAndLoad(context).setValue(today.element)

        // There may be multiple widgets active, so update all of them
        val widgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, ShortCommandWidget::class.java))
        for (appWidgetId in widgetIds) {

            // Construct the RemoteViews object
            val remoteViews = RemoteViews(context.packageName, R.layout.view_short_command_widget)

            remoteViews.setTextViewText(
                R.id.tvWidgetName,
                "${today.short} — ${ElementTranslator.getLocalizedName(context, today.element)}",
            )
            remoteViews.setTextViewText(
                R.id.tvWidgetFact,
                ElementWeightCache.getFact(today.short) ?: "",
            )

            // FIX-023: flWidgetSearchBar only exists in layout-v31 (API 31+) — the default
            // layout (API < 31) has no such id, so setOnClickPendingIntent() on it was a
            // silent no-op there, leaving the widget dead on Android <= 11. widgetRoot exists
            // in both layout variants, so binding the click there works on every API level.
            //Open element-of-the-day detail on Widget Click
            remoteViews.setOnClickPendingIntent(
                R.id.widgetRoot,
                PendingIntent.getActivity(
                    /* context = */ context,
                    /* requestCode = */ 0,
                    /* intent = */ Intent(context, ElementInfoAct::class.java),
                    /* flags = */ PendingIntent.FLAG_IMMUTABLE
                )
            )

            //Update Widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
