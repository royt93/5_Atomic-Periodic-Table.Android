package com.mckimquyen.atomicPeriodicTable.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mckimquyen.atomicPeriodicTable.feature.trivia.TriviaWidgetPref
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.util.ElementOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso can't drive RemoteViews directly (no view hierarchy to query on a real home screen
 * widget host), so correctness here is verified through the persisted TriviaWidgetPref state
 * that updateSingleWidget()/onReceive() write as an observable side effect — the same signal
 * ShortCommandWidget itself relies on to avoid reshuffling answers mid-day.
 */
@RunWith(AndroidJUnit4::class)
class ShortCommandWidgetTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val widget = ShortCommandWidget()

    @Before
    fun clearState() {
        context.getSharedPreferences("Trivia_Widget_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testWidgetProviderInstantiation() {
        assertNotNull("Widget provider should not be null", widget)
    }

    @Test
    fun testWidgetOnUpdateDoesNotCrash() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        // Ensure calling onUpdate does not throw any exceptions
        widget.onUpdate(context, appWidgetManager, intArrayOf(1, 2))
    }

    // Regression guard for the Element-of-the-Day widget: onUpdate() must point
    // ElementSendAndLoad (read by ElementInfoAct on launch) at the same element
    // ElementOfDay.indexForDay() picks for today, so tapping the widget opens the
    // element it's actually displaying, not whatever was last viewed in the app.
    @Test
    fun onUpdate_pointsElementSendAndLoad_atTodaysElement() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        widget.onUpdate(context, appWidgetManager, intArrayOf(1))

        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val expected = elements[ElementOfDay.indexForDay(System.currentTimeMillis() / 86_400_000L, elements.size)]

        assertEquals(expected.element, ElementSendAndLoad(context).getValue())
    }

    private fun invokeUpdateSingleWidget(appWidgetId: Int) {
        val method = ShortCommandWidget::class.java.getDeclaredMethod(
            "updateSingleWidget",
            android.content.Context::class.java,
            AppWidgetManager::class.java,
            Int::class.java,
        )
        method.isAccessible = true
        method.invoke(widget, context, AppWidgetManager.getInstance(context), appWidgetId)
    }

    @Test
    fun updateSingleWidget_generatesAndPersistsTodaysQuestion() {
        invokeUpdateSingleWidget(appWidgetId = 1001)

        val state = TriviaWidgetPref(context).getState()
        assertNotNull("expected a trivia question to be generated for today", state)
        assertEquals(4, state!!.choices.size)
        assertEquals(4, state.choices.toSet().size)
        assertTrue(state.choices.contains(state.correctSymbol))
        assertTrue(state.fact.isNotBlank())
        assertNull("must start unanswered", state.answeredCorrect)
    }

    @Test
    fun callingUpdateSingleWidgetTwiceSameDay_doesNotReshuffleChoices() {
        invokeUpdateSingleWidget(appWidgetId = 1002)
        val first = TriviaWidgetPref(context).getState()
        requireNotNull(first)

        invokeUpdateSingleWidget(appWidgetId = 1002) // e.g. triggered by a widget resize

        val second = TriviaWidgetPref(context).getState()
        assertEquals(first, second)
    }

    @Test
    fun onReceive_withCorrectAnswer_marksAnsweredCorrectTrue() {
        invokeUpdateSingleWidget(appWidgetId = 1003)
        val state = requireNotNull(TriviaWidgetPref(context).getState())

        val intent = Intent(context, ShortCommandWidget::class.java).apply {
            action = ShortCommandWidget.ACTION_ANSWER
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1003)
            putExtra(ShortCommandWidget.EXTRA_CHOSEN_SYMBOL, state.correctSymbol)
        }
        widget.onReceive(context, intent)

        assertEquals(true, TriviaWidgetPref(context).getState()?.answeredCorrect)
    }

    @Test
    fun onReceive_withWrongAnswer_marksAnsweredCorrectFalse() {
        invokeUpdateSingleWidget(appWidgetId = 1004)
        val state = requireNotNull(TriviaWidgetPref(context).getState())
        val wrongSymbol = state.choices.first { it != state.correctSymbol }

        val intent = Intent(context, ShortCommandWidget::class.java).apply {
            action = ShortCommandWidget.ACTION_ANSWER
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1004)
            putExtra(ShortCommandWidget.EXTRA_CHOSEN_SYMBOL, wrongSymbol)
        }
        widget.onReceive(context, intent)

        assertEquals(false, TriviaWidgetPref(context).getState()?.answeredCorrect)
    }

    @Test
    fun onReceive_secondAnswerSameDay_isIgnored_keepsFirstResult() {
        invokeUpdateSingleWidget(appWidgetId = 1005)
        val state = requireNotNull(TriviaWidgetPref(context).getState())
        val wrongSymbol = state.choices.first { it != state.correctSymbol }

        fun answerWith(symbol: String) {
            widget.onReceive(
                context,
                Intent(context, ShortCommandWidget::class.java).apply {
                    action = ShortCommandWidget.ACTION_ANSWER
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1005)
                    putExtra(ShortCommandWidget.EXTRA_CHOSEN_SYMBOL, symbol)
                },
            )
        }

        answerWith(state.correctSymbol) // first tap: correct
        answerWith(wrongSymbol) // stale second tap: must be ignored

        assertEquals(true, TriviaWidgetPref(context).getState()?.answeredCorrect)
    }
}
