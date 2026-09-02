package com.mckimquyen.atomicPeriodicTable.feature.trivia

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TriviaWidgetPrefTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    fun clear() {
        context.getSharedPreferences("Trivia_Widget_Preference", android.content.Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun noStateYet_returnsNull() {
        assertNull(TriviaWidgetPref(context).getState())
    }

    @Test
    fun savedState_persistsAcrossInstances() {
        val state = TriviaWidgetState(
            epochDay = 20000L,
            fact = "A noble gas.",
            correctSymbol = "He",
            choices = listOf("H", "He", "Li", "Be"),
            answeredCorrect = null,
        )
        TriviaWidgetPref(context).saveState(state)

        val reread = TriviaWidgetPref(context).getState()
        assertEquals(state, reread)
    }

    @Test
    fun updatingAnsweredCorrect_overwritesPreviousState() {
        val pref = TriviaWidgetPref(context)
        val initial = TriviaWidgetState(20000L, "A noble gas.", "He", listOf("H", "He", "Li", "Be"))
        pref.saveState(initial)

        pref.saveState(initial.copy(answeredCorrect = true))

        assertEquals(true, pref.getState()?.answeredCorrect)
    }
}
