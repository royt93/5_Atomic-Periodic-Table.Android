package com.mckimquyen.atomicPeriodicTable.util

import kotlin.math.abs

object ChemicalEquationBalancer {

    class Fraction(num: Long, den: Long = 1L) {
        val numerator: Long
        val denominator: Long

        init {
            var n = num
            var d = den
            if (d == 0L) throw ArithmeticException("Division by zero")
            if (d < 0L) {
                n = -n
                d = -d
            }
            val g = gcd(abs(n), d)
            numerator = n / g
            denominator = d / g
        }

        private fun gcd(a: Long, b: Long): Long {
            var x = a
            var y = b
            while (y != 0L) {
                val temp = y
                y = x % y
                x = temp
            }
            return x
        }

        operator fun plus(o: Fraction) = Fraction(numerator * o.denominator + o.numerator * denominator, denominator * o.denominator)
        operator fun minus(o: Fraction) = Fraction(numerator * o.denominator - o.numerator * denominator, denominator * o.denominator)
        operator fun times(o: Fraction) = Fraction(numerator * o.numerator, denominator * o.denominator)
        operator fun div(o: Fraction) = Fraction(numerator * o.denominator, denominator * o.numerator)

        fun isZero() = numerator == 0L
        override fun toString() = if (denominator == 1L) "$numerator" else "$numerator/$denominator"
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    private fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }

    data class EquationResult(
        val reactants: List<String>,
        val products: List<String>,
        val reactantCoefficients: List<Long>,
        val productCoefficients: List<Long>,
        val balancedString: String
    )

    /**
     * Balances a chemical equation string. E.g., "H2 + O2 = H2O" or "Fe + Cl2 -> FeCl3"
     * Returns EquationResult, or null if cannot be balanced.
     */
    fun balance(equation: String): EquationResult? {
        val parts = equation.split("=", "->")
        if (parts.size != 2) return null

        val reactantsStr = parts[0].trim()
        val productsStr = parts[1].trim()

        val reactants = reactantsStr.split("+").map { it.trim() }.filter { it.isNotEmpty() }
        val products = productsStr.split("+").map { it.trim() }.filter { it.isNotEmpty() }

        if (reactants.isEmpty() || products.isEmpty()) return null

        // Parse all compounds
        val reactantParsed = mutableListOf<Map<String, Int>>()
        val productParsed = mutableListOf<Map<String, Int>>()

        try {
            for (r in reactants) {
                reactantParsed.add(ChemicalFormulaParser.parse(r))
            }
            for (p in products) {
                productParsed.add(ChemicalFormulaParser.parse(p))
            }
        } catch (e: Exception) {
            return null
        }

        // Get unique elements
        val allElements = mutableSetOf<String>()
        reactantParsed.forEach { allElements.addAll(it.keys) }
        productParsed.forEach { allElements.addAll(it.keys) }

        // Elements on left and right sides must match
        val reactantElements = reactantParsed.flatMap { it.keys }.toSet()
        val productElements = productParsed.flatMap { it.keys }.toSet()
        if (reactantElements != productElements) {
            return null
        }

        val numCompounds = reactants.size + products.size
        val elementsList = allElements.toList()
        val numElements = elementsList.size

        // Build the system of equations matrix A of size numElements x numCompounds
        // Let x_1...x_n be reactant coefficients, x_{n+1}...x_{n+m} be product coefficients
        // Equation for element E: sum(x_i * count(E, R_i)) - sum(x_j * count(E, P_j)) = 0
        val matrix = Array(numElements) { Array(numCompounds) { Fraction(0) } }

        for (i in 0 until numElements) {
            val element = elementsList[i]
            // Reactants (positive)
            for (j in reactants.indices) {
                val count = reactantParsed[j][element] ?: 0
                matrix[i][j] = Fraction(count.toLong())
            }
            // Products (negative)
            for (j in products.indices) {
                val count = productParsed[j][element] ?: 0
                matrix[i][reactants.size + j] = Fraction(-count.toLong())
            }
        }

        // We assume x_N = 1, so the system becomes A' * X' = B where B is the negative of the last column of A
        // A' has size numElements x (numCompounds - 1)
        val numVars = numCompounds - 1
        val aug = Array(numElements) { Array(numVars + 1) { Fraction(0) } }

        for (i in 0 until numElements) {
            for (j in 0 until numVars) {
                aug[i][j] = matrix[i][j]
            }
            // B = -matrix[i][numCompounds - 1]
            aug[i][numVars] = Fraction(0L) - matrix[i][numCompounds - 1]
        }

        // Solve using Gaussian elimination
        var row = 0
        for (col in 0 until numVars) {
            // Find pivot
            var pivotRow = -1
            for (r in row until numElements) {
                if (!aug[r][col].isZero()) {
                    pivotRow = r
                    break
                }
            }

            if (pivotRow == -1) {
                // No unique solution or underdetermined
                continue
            }

            // Swap rows
            val temp = aug[row]
            aug[row] = aug[pivotRow]
            aug[pivotRow] = temp

            // Normalize pivot row
            val pivotVal = aug[row][col]
            for (c in col..numVars) {
                aug[row][c] = aug[row][c] / pivotVal
            }

            // Eliminate column in other rows
            for (r in 0 until numElements) {
                if (r != row && !aug[r][col].isZero()) {
                    val factor = aug[r][col]
                    for (c in col..numVars) {
                        aug[r][c] = aug[r][c] - (factor * aug[row][c])
                    }
                }
            }
            row++
        }

        // Back-substitution / verification of consistency
        // If there are rows of the form 0 = non-zero, the system is inconsistent (unbalanceable)
        for (r in row until numElements) {
            if (!aug[r][numVars].isZero()) {
                return null
            }
        }

        // Read off variables
        val solutionFractions = mutableListOf<Fraction>()
        for (col in 0 until numVars) {
            // Find the row where this variable's leading coefficient is 1
            var foundRow = -1
            for (r in 0 until numElements) {
                if (r < row && aug[r][col].numerator == 1L && aug[r][col].denominator == 1L) {
                    // Make sure it is indeed the leading coefficient
                    var isLeading = true
                    for (c in 0 until col) {
                        if (!aug[r][c].isZero()) {
                            isLeading = false
                            break
                        }
                    }
                    if (isLeading) {
                        foundRow = r
                        break
                    }
                }
            }
            if (foundRow == -1) {
                // Free variable or no unique solution
                return null
            }
            solutionFractions.add(aug[foundRow][numVars])
        }
        // Add x_N = 1
        solutionFractions.add(Fraction(1L))

        // Check that all solved coefficients are positive
        for (f in solutionFractions) {
            if (f.numerator <= 0L) {
                return null
            }
        }

        // Multiply by LCM of all denominators to make them integers
        var currentLCM = 1L
        for (f in solutionFractions) {
            currentLCM = lcm(currentLCM, f.denominator)
        }

        val integerCoefficients = solutionFractions.map { f ->
            val num = f.numerator * (currentLCM / f.denominator)
            num
        }

        // Divide by GCD of all coefficients to simplify to smallest integers
        var currentGCD = integerCoefficients[0]
        for (coef in integerCoefficients) {
            currentGCD = gcd(currentGCD, coef)
        }

        val finalCoefficients = integerCoefficients.map { it / currentGCD }

        // Build result string
        val reactantCoeffs = finalCoefficients.subList(0, reactants.size)
        val productCoeffs = finalCoefficients.subList(reactants.size, finalCoefficients.size)

        val balancedReactants = reactants.indices.joinToString(" + ") { idx ->
            val coef = reactantCoeffs[idx]
            val coefStr = if (coef == 1L) "" else "$coef "
            "$coefStr${reactants[idx]}"
        }

        val balancedProducts = products.indices.joinToString(" + ") { idx ->
            val coef = productCoeffs[idx]
            val coefStr = if (coef == 1L) "" else "$coef "
            "$coefStr${products[idx]}"
        }

        val balancedStr = "$balancedReactants = $balancedProducts"

        return EquationResult(
            reactants = reactants,
            products = products,
            reactantCoefficients = reactantCoeffs,
            productCoefficients = productCoeffs,
            balancedString = balancedStr
        )
    }
}
