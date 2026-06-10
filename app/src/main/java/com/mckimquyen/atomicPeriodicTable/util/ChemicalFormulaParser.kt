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

        // Recursive DFS with backtracking: returns the normalized remainder starting at
        // [index], or null if no valid element tokenization exists from here. The caller
        // tries the next candidate when a branch dead-ends.
        fun dfs(index: Int): String? {
            if (index == clean.length) return ""

            val c = clean[index]
            // Digits and brackets are passed through unchanged.
            if (c.isDigit() || c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') {
                val rest = dfs(index + 1) ?: return null
                return c + rest
            }
            if (!c.isLetter()) return null

            val next = if (index + 1 < clean.length) clean[index + 1] else null
            val twoLower = if (next != null && next.isLetter()) clean.substring(index, index + 2).lowercase() else null
            val oneLower = c.lowercaseChar().toString()

            // Candidate symbols to try, in priority order: Pair(canonicalSymbol, charsConsumed).
            val candidates = ArrayList<Pair<String, Int>>()

            if (c.isUpperCase()) {
                // Proper/mixed case: an uppercase letter always begins a new symbol.
                // Merge into a 2-char symbol ONLY when the following letter is lowercase
                // (e.g. "Co" -> Cobalt, "Nh" -> Nihonium). Two uppercase letters like the
                // N and H in "NH3" therefore stay as separate elements (N + H).
                if (next != null && next.isLowerCase() && twoLower != null && symbolMap.containsKey(twoLower)) {
                    candidates.add(symbolMap[twoLower]!! to 2)
                }
                if (symbolMap.containsKey(oneLower)) {
                    candidates.add(symbolMap[oneLower]!! to 1)
                }
            } else {
                // All-lowercase (ambiguous) input: keep the legacy heuristic — prefer the
                // 2-char symbol first unless it is one of the exceptions, which prefer 1-char.
                val twoValid = twoLower != null && symbolMap.containsKey(twoLower)
                val oneValid = symbolMap.containsKey(oneLower)
                if (twoValid && exceptions.contains(twoLower)) {
                    if (oneValid) candidates.add(symbolMap[oneLower]!! to 1)
                    candidates.add(symbolMap[twoLower]!! to 2)
                } else {
                    if (twoValid) candidates.add(symbolMap[twoLower]!! to 2)
                    if (oneValid) candidates.add(symbolMap[oneLower]!! to 1)
                }
            }

            for ((symbol, consumed) in candidates) {
                val rest = dfs(index + consumed)
                if (rest != null) return symbol + rest
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
