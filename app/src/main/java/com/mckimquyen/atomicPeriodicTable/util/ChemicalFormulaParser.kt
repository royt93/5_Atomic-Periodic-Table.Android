package com.mckimquyen.atomicPeriodicTable.util

import java.util.Stack

object ChemicalFormulaParser {

    private val ELEMENT_SYMBOLS = setOf(
        "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar",
        "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr",
        "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe",
        "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu",
        "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra",
        "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db",
        "Sg", "Bh", "Hs", "Mt", "Ds", "Rg", "Cn", "Nh", "Fl", "Mc", "Lv", "Ts", "Og"
    )

    /**
     * Normalizes case in chemical formula strings (e.g. h2o -> H2O, nacl -> NaCl, co2 -> CO2).
     */
    fun normalize(formula: String): String {
        val clean = formula.replace(" ", "")
        if (clean.isEmpty()) return ""

        val symbolMap = ELEMENT_SYMBOLS.associateBy { it.lowercase() }
        val exceptions = setOf("co", "no", "po", "cs", "ni", "bi", "cn")

        fun dfs(index: Int): String? {
            if (index == clean.length) return ""

            val c = clean[index]
            if (c.isDigit() || c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') {
                val rest = dfs(index + 1)
                return if (rest != null) c + rest else null
            }

            // Try 2-character match first unless it's in exceptions
            if (index + 1 < clean.length && clean[index].isLetter() && clean[index + 1].isLetter()) {
                val twoCharLower = clean.substring(index, index + 2).lowercase()
                if (symbolMap.containsKey(twoCharLower)) {
                    if (exceptions.contains(twoCharLower)) {
                        // Try 1-character first
                        val oneCharLower = c.toString().lowercase()
                        if (symbolMap.containsKey(oneCharLower)) {
                            val rest = dfs(index + 1)
                            if (rest != null) {
                                return symbolMap[oneCharLower]!! + rest
                            }
                        }
                        // Fallback to 2-character if 1-character fails
                        val rest = dfs(index + 2)
                        if (rest != null) {
                            return symbolMap[twoCharLower]!! + rest
                        }
                    } else {
                        // Try 2-character first
                        val rest = dfs(index + 2)
                        if (rest != null) {
                            return symbolMap[twoCharLower]!! + rest
                        }
                        // Fallback to 1-character if 2-character fails
                        val oneCharLower = c.toString().lowercase()
                        if (symbolMap.containsKey(oneCharLower)) {
                            val rest = dfs(index + 1)
                            if (rest != null) {
                                return symbolMap[oneCharLower]!! + rest
                            }
                        }
                    }
                }
            }

            // Try 1-character match
            if (c.isLetter()) {
                val oneCharLower = c.toString().lowercase()
                if (symbolMap.containsKey(oneCharLower)) {
                    val rest = dfs(index + 1)
                    if (rest != null) {
                        return symbolMap[oneCharLower]!! + rest
                    }
                }
            }

            return null
        }

        return dfs(0) ?: formula
    }

    /**
     * Parses a chemical formula string and returns a map of Element Symbol -> Count.
     * Throws IllegalArgumentException if the formula is malformed.
     */
    fun parse(formula: String): Map<String, Int> {
        val normalized = normalize(formula)
        val cleanFormula = normalized.replace(" ", "")
        if (cleanFormula.isEmpty()) {
            throw IllegalArgumentException("Formula cannot be empty")
        }

        val stack = Stack<MutableMap<String, Int>>()
        stack.push(mutableMapOf())

        var i = 0
        val len = cleanFormula.length

        while (i < len) {
            val c = cleanFormula[i]
            when {
                c == '(' || c == '[' || c == '{' -> {
                    stack.push(mutableMapOf())
                    i++
                }
                c == ')' || c == ']' || c == '}' -> {
                    if (stack.size <= 1) {
                        throw IllegalArgumentException("Mismatched closing bracket at index $i")
                    }
                    i++
                    // Read multiplier after bracket
                    var multStr = ""
                    while (i < len && cleanFormula[i].isDigit()) {
                        multStr += cleanFormula[i]
                        i++
                    }
                    val multiplier = if (multStr.isNotEmpty()) multStr.toInt() else 1

                    val popped = stack.pop()
                    val currentTop = stack.peek()
                    for ((element, count) in popped) {
                        currentTop[element] = (currentTop[element] ?: 0) + count * multiplier
                    }
                }
                c.isUpperCase() -> {
                    // Read element symbol
                    var symbol = c.toString()
                    i++
                    while (i < len && cleanFormula[i].isLowerCase()) {
                        symbol += cleanFormula[i]
                        i++
                    }

                    // Read count
                    var countStr = ""
                    while (i < len && cleanFormula[i].isDigit()) {
                        countStr += cleanFormula[i]
                        i++
                    }
                    val count = if (countStr.isNotEmpty()) countStr.toInt() else 1

                    val currentTop = stack.peek()
                    currentTop[symbol] = (currentTop[symbol] ?: 0) + count
                }
                else -> {
                    throw IllegalArgumentException("Unexpected character '$c' at index $i")
                }
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Mismatched opening bracket")
        }

        val result = stack.pop()
        if (result.isEmpty()) {
            throw IllegalArgumentException("Formula contains no elements")
        }
        return result
    }
}
