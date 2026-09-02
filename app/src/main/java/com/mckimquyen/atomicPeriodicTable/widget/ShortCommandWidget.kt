package com.mckimquyen.atomicPeriodicTable.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.ElementInfoAct
import com.mckimquyen.atomicPeriodicTable.feature.trivia.TriviaQuestionGenerator
import com.mckimquyen.atomicPeriodicTable.feature.trivia.TriviaWidgetPref
import com.mckimquyen.atomicPeriodicTable.feature.trivia.TriviaWidgetState
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.util.ElementOfDay
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache
import java.util.Random

/**
 * Implementation of App Widget functionality.
 * Shows an "Element of the Day" quick-trivia question (mục 20, vòng 5): a 1-sentence fact about
 * the day's element (see ElementOfDay) plus 4 tappable answer choices, refreshed by the platform
 * every 24h via android:updatePeriodMillis (no WorkManager needed).
 */
class ShortCommandWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ANSWER) {
            handleAnswer(context, intent)
            return
        }
        super.onReceive(context, intent)
    }

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
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun handleAnswer(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val chosenSymbol = intent.getStringExtra(EXTRA_CHOSEN_SYMBOL)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || chosenSymbol == null) return

        val pref = TriviaWidgetPref(context)
        val state = pref.getState() ?: return
        if (state.answeredCorrect != null) return // already answered today — ignore a stale tap

        pref.saveState(state.copy(answeredCorrect = chosenSymbol == state.correctSymbol))
        updateSingleWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
    }

    private fun updateSingleWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        ElementWeightCache.init(context)

        val todayEpochDay = System.currentTimeMillis() / 86_400_000L
        val today = elements[ElementOfDay.indexForDay(todayEpochDay, elements.size)]
        ElementSendAndLoad(context).setValue(today.element)

        val pref = TriviaWidgetPref(context)
        var state = pref.getState()
        if (state == null || state.epochDay != todayEpochDay) {
            val question = TriviaQuestionGenerator.generate(
                correctSymbol = today.short,
                allSymbols = elements.map { it.short },
                factLookup = { symbol -> ElementWeightCache.getFact(symbol) },
                random = Random(),
            )
            state = question?.let {
                TriviaWidgetState(todayEpochDay, it.fact, it.correctSymbol, it.choices)
            }
            state?.let { pref.saveState(it) }
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.view_short_command_widget)
        remoteViews.setTextViewText(
            R.id.tvWidgetName,
            "${today.short} — ${ElementTranslator.getLocalizedName(context, today.element)}",
        )

        val answerButtonIds = listOf(R.id.widgetAnswer1, R.id.widgetAnswer2, R.id.widgetAnswer3, R.id.widgetAnswer4)

        if (state == null) {
            // Fallback: no fact data available for today's element — degrade to the original
            // display-only behavior rather than showing a broken quiz.
            remoteViews.setTextViewText(R.id.tvWidgetFact, ElementWeightCache.getFact(today.short) ?: "")
            remoteViews.setViewVisibility(R.id.widgetAnswersRow, View.GONE)
            remoteViews.setViewVisibility(R.id.tvWidgetResult, View.GONE)
        } else {
            remoteViews.setTextViewText(R.id.tvWidgetFact, state.fact)
            val answeredCorrect = state.answeredCorrect
            if (answeredCorrect == null) {
                remoteViews.setViewVisibility(R.id.widgetAnswersRow, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.tvWidgetResult, View.GONE)
                state.choices.forEachIndexed { index, symbol ->
                    remoteViews.setTextViewText(answerButtonIds[index], displayNameFor(context, symbol))
                    remoteViews.setOnClickPendingIntent(
                        answerButtonIds[index],
                        PendingIntent.getBroadcast(
                            context,
                            appWidgetId * 10 + index, // distinct request code per widget+answer slot
                            Intent(context, ShortCommandWidget::class.java).apply {
                                action = ACTION_ANSWER
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                putExtra(EXTRA_CHOSEN_SYMBOL, symbol)
                            },
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        ),
                    )
                }
            } else {
                remoteViews.setViewVisibility(R.id.widgetAnswersRow, View.GONE)
                remoteViews.setViewVisibility(R.id.tvWidgetResult, View.VISIBLE)
                remoteViews.setTextViewText(
                    R.id.tvWidgetResult,
                    if (answeredCorrect) {
                        context.getString(R.string.widget_trivia_correct)
                    } else {
                        context.getString(R.string.widget_trivia_wrong, displayNameFor(context, state.correctSymbol))
                    },
                )
            }
        }

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

    private fun displayNameFor(context: Context, symbol: String): String {
        val rawName = ElementWeightCache.getName(symbol) ?: symbol
        return ElementTranslator.getLocalizedName(context, rawName)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        const val ACTION_ANSWER = "com.mckimquyen.atomicPeriodicTable.widget.ACTION_TRIVIA_ANSWER"
        const val EXTRA_CHOSEN_SYMBOL = "extra_chosen_symbol"
    }
}
