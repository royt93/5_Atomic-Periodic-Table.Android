package com.mckimquyen.atomicPeriodicTable.util

import java.util.Stack

object ChemicalFormulaParser {

    /**
     * Parses a chemical formula string and returns a map of Element Symbol -> Count.
     * Throws IllegalArgumentException if the formula is malformed.
     */
    fun parse(formula: String): Map<String, Int> {
        val cleanFormula = formula.replace(" ", "")
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
