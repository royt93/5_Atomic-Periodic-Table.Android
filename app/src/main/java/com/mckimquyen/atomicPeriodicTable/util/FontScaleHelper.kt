package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import android.content.res.Configuration
import com.mckimquyen.atomicPeriodicTable.pref.FontScalePref

object FontScaleHelper {

    /** Pure decision: maps the stored preference value to a multiplier, JVM-testable without Context. */
    fun multiplierFor(prefValue: Int): Float = when (prefValue) {
        FontScalePref.SMALL -> 0.85f
        FontScalePref.LARGE -> 1.15f
        else -> 1.0f
    }

    /**
     * Applies the app's font-scale preference ON TOP OF (never instead of) the system's own
     * accessibility font-scale setting — multiplies, never replaces. See
     * LocaleHelper.setLocale()'s "FIX (P3)" comment for the exact bug this must not reintroduce:
     * a prior version of this codebase forced fontScale to a fixed value and silently wiped out
     * the user's system-wide accessibility font-size setting every time the language changed.
     */
    fun applyFontScale(context: Context): Context {
        val multiplier = multiplierFor(FontScalePref(context).getValue())
        if (multiplier == 1.0f) return context

        val config = Configuration(context.resources.configuration)
        config.fontScale = context.resources.configuration.fontScale * multiplier
        return context.createConfigurationContext(config)
    }
}
