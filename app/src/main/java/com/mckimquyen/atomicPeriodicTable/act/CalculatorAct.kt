package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ACalculatorBinding
import com.mckimquyen.atomicPeriodicTable.databinding.ItemCompositionBreakdownBinding
import com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache
import com.mckimquyen.atomicPeriodicTable.util.Utils

class CalculatorAct : BaseAct() {

    private lateinit var binding: ACalculatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ACalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.commonTitleBack.layoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBack.layoutParams = params

        val topPadding = resources.getDimensionPixelSize(R.dimen.margin)
        val bottomPadding = bottom + resources.getDimensionPixelSize(R.dimen.margin)
        binding.calcScrollView.setPadding(0, topPadding, 0, bottomPadding)
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.calcBtn.setOnClickListener {
            performCalculation()
        }

        binding.calcInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                performCalculation()
                true
            } else {
                false
            }
        }
    }

    private fun performCalculation() {
        // Clear previous state
        binding.calcInputLayout.error = null
        binding.calcResultCard.visibility = View.GONE
        binding.breakdownContainer.removeAllViews()

        val input = binding.calcInput.text.toString().trim()
        if (input.isEmpty()) {
            return
        }

        try {
            val parsedMap = ChemicalFormulaParser.parse(input)
            
            // Validate all elements first
            var totalMass = 0.0
            val elementMassList = mutableListOf<Triple<String, Int, Double>>() // Symbol, Count, ElementMass

            for ((symbol, count) in parsedMap) {
                val mass = ElementWeightCache.getMass(symbol)
                if (mass == null || mass == 0.0) {
                    binding.calcInputLayout.error = getString(R.string.molar_mass_error_unknown_element).format(symbol)
                    return
                }
                val elementContribution = mass * count
                totalMass += elementContribution
                elementMassList.add(Triple(symbol, count, mass))
            }

            if (totalMass <= 0.0) {
                binding.calcInputLayout.error = getString(R.string.molar_mass_error)
                return
            }

            // Display overall results
            binding.calcResultText.text = getString(R.string.molar_mass_result).format(totalMass)
            binding.calcResultCard.visibility = View.VISIBLE
            Utils.slideUpFadeIn(binding.calcResultCard, 400)

            // Populate composition breakdown
            for ((symbol, count, mass) in elementMassList) {
                val elementContribution = mass * count
                val percentage = (elementContribution / totalMass) * 100.0

                val rowBinding = ItemCompositionBreakdownBinding.inflate(layoutInflater, binding.breakdownContainer, false)
                
                // Get element localized name if possible
                val englishName = ElementWeightCache.getName(symbol) ?: ""
                val capitalizedName = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(this, englishName)
                
                rowBinding.elSymbolName.text = "$symbol - $capitalizedName"
                rowBinding.elPercentage.text = "%.2f%%".format(percentage)
                rowBinding.elDetailFormula.text = "%d atoms × %.4f g/mol = %.4f g/mol".format(count, mass, elementContribution)
                rowBinding.elProgressBar.progress = percentage.toInt()

                binding.breakdownContainer.addView(rowBinding.root)
            }

        } catch (e: Exception) {
            binding.calcInputLayout.error = getString(R.string.molar_mass_error)
        }
    }
}
