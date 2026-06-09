package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import com.mckimquyen.atomicPeriodicTable.R

object CategoryTranslator {
    fun translate(context: Context, category: String): String {
        val clean = category.trim()
        return when {
            clean.equals("Other Nonmetals", ignoreCase = true) -> context.getString(R.string.cat_other_nonmetals)
            clean.equals("Noble Gases", ignoreCase = true) -> context.getString(R.string.cat_noble_gases)
            clean.equals("Alkali Metals", ignoreCase = true) -> context.getString(R.string.cat_alkali_metals)
            clean.equals("Alkaline Earth Metals", ignoreCase = true) -> context.getString(R.string.cat_alkaline_earth)
            clean.equals("Transition Metals", ignoreCase = true) -> context.getString(R.string.cat_transition_metals)
            clean.equals("Lanthanides", ignoreCase = true) -> context.getString(R.string.cat_lanthanides)
            clean.equals("Actinides", ignoreCase = true) -> context.getString(R.string.cat_actinides)
            clean.equals("Post-transition Metals", ignoreCase = true) -> context.getString(R.string.cat_post_transition)
            clean.equals("Metalloids", ignoreCase = true) -> context.getString(R.string.cat_metalloids)
            clean.equals("Halogens", ignoreCase = true) -> context.getString(R.string.cat_halogens)
            clean.equals("Reactive Nonmetals", ignoreCase = true) -> context.getString(R.string.cat_reactive_nonmetals)
            else -> category
        }
    }
}
