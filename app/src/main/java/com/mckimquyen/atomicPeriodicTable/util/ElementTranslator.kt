package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import java.util.Locale

object ElementTranslator {

    fun getCleanName(englishName: String): String {
        return englishName.lowercase(Locale.US).trim().replace(" ", "_")
    }

    fun getLocalizedName(context: Context, englishName: String): String {
        val cleanName = getCleanName(englishName)
        val key = "element_name_$cleanName"
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) {
            return context.getString(resId)
        }
        // Fallback to capitalized English name if resource is not found
        return englishName.trim().replaceFirstChar { it.uppercase() }
    }
}
