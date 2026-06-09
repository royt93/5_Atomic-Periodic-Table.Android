package com.mckimquyen.atomicPeriodicTable.util

import android.content.Context
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import org.json.JSONArray
import java.io.InputStream

object ElementWeightCache {
    private val cache = mutableMapOf<String, Double>() // Symbol -> Mass
    private val symbolToName = mutableMapOf<String, String>() // Symbol -> Name (lowercase)
    private val categoryCache = mutableMapOf<String, String>() // Symbol -> Category

    fun init(context: Context) {
        if (cache.isNotEmpty()) return
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        for (el in elements) {
            try {
                val inputStream: InputStream = context.assets.open("${el.element}.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val jsonObject = jsonArray.getJSONObject(0)
                
                // Mass
                val massStr = jsonObject.optString("element_atomicmass", "0")
                var cleanMassStr = massStr.replace("[", "").replace("]", "").trim()
                val spaceIndex = cleanMassStr.indexOf(' ')
                if (spaceIndex != -1) {
                    cleanMassStr = cleanMassStr.substring(0, spaceIndex)
                }
                val parenIndex = cleanMassStr.indexOf('(')
                if (parenIndex != -1) {
                    cleanMassStr = cleanMassStr.substring(0, parenIndex)
                }
                val cleanMass = cleanMassStr.trim().toDoubleOrNull() ?: 0.0
                cache[el.short] = cleanMass
                
                // Name
                symbolToName[el.short] = el.element
                
                // Category (element_group in JSON)
                val category = jsonObject.optString("element_group", "---")
                categoryCache[el.short] = category
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getMass(symbol: String): Double? {
        return cache[symbol]
    }

    fun getName(symbol: String): String? {
        return symbolToName[symbol]
    }

    fun getCategory(symbol: String): String? {
        return categoryCache[symbol]
    }

    fun isValidSymbol(symbol: String): Boolean {
        return cache.containsKey(symbol)
    }

    fun getAllSymbols(): List<String> {
        return cache.keys.toList()
    }
}
