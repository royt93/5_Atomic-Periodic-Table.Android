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
import com.mckimquyen.atomicPeriodicTable.act.MainAct

/**
 * Implementation of App Widget functionality.
 */
class ShortCommandWidget : AppWidgetProvider() {
    @SuppressLint("RemoteViewLayout")
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {

        // There may be multiple widgets active, so update all of them
        val widgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, ShortCommandWidget::class.java))
        for (appWidgetId in widgetIds) {

            // Construct the RemoteViews object
            val remoteViews = RemoteViews(context.packageName, R.layout.view_short_command_widget)

            //Open App on Widget Click
            remoteViews.setOnClickPendingIntent(
                R.id.flWidgetSearchBar,
                PendingIntent.getActivity(
                    /* context = */ context,
                    /* requestCode = */ 0,
                    /* intent = */ Intent(context, MainAct::class.java),
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
