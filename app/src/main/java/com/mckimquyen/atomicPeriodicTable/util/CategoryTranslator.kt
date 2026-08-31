package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import com.mckimquyen.atomicPeriodicTable.R

object CategoryTranslator {
    fun translate(context: Context, category: String): String {
        val clean = category.trim().lowercase()
        return when {
            clean in listOf("other nonmetal", "other nonmetals") -> context.getString(R.string.cat_other_nonmetals)
            clean in listOf("noble gas", "noble gases") -> context.getString(R.string.cat_noble_gases)
            clean in listOf("alkali metal", "alkali metals") -> context.getString(R.string.cat_alkali_metals)
            clean in listOf("alkaline earth metal", "alkaline earth metals", "alkaline earth") -> context.getString(R.string.cat_alkaline_earth)
            clean in listOf("transition metal", "transition metals") -> context.getString(R.string.cat_transition_metals)
            clean in listOf("lanthanide", "lanthanides", "lanthanoid", "lanthanoids") -> context.getString(R.string.cat_lanthanides)
            clean in listOf("actinide", "actinides", "actinoid", "actinoids") -> context.getString(R.string.cat_actinides)
            clean in listOf("post-transition metal", "post-transition metals", "post transition metal", "post transition metals") -> context.getString(R.string.cat_post_transition)
            clean in listOf("metalloid", "metalloids") -> context.getString(R.string.cat_metalloids)
            clean in listOf("halogen", "halogens") -> context.getString(R.string.cat_halogens)
            clean in listOf("reactive nonmetal", "reactive nonmetals", "diatomic nonmetal", "polyatomic nonmetal") -> context.getString(R.string.cat_reactive_nonmetals)
            else -> category
        }
    }
}
