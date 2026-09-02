package com.mckimquyen.atomicPeriodicTable.feature.trivia

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson

/**
 * [answeredCorrect] null = not answered yet today. Persisting the whole state (not just the
 * correct answer) means the 4 choices are fixed for the day once generated — regenerating them
 * on every onUpdate() (e.g. a widget resize) would otherwise silently reshuffle the answers the
 * user is looking at.
 */
data class TriviaWidgetState(
    val epochDay: Long,
    val fact: String,
    val correctSymbol: String,
    val choices: List<String>,
    val answeredCorrect: Boolean? = null,
)

class TriviaWidgetPref(context: Context) {
    private val preference = context.getSharedPreferences("Trivia_Widget_Preference", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getState(): TriviaWidgetState? {
        val json = preference.getString(KEY_STATE, null) ?: return null
        return try {
            gson.fromJson(json, TriviaWidgetState::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveState(state: TriviaWidgetState) {
        preference.edit { putString(KEY_STATE, gson.toJson(state)) }
    }

    companion object {
        private const val KEY_STATE = "trivia_widget_state_json"
    }
}
