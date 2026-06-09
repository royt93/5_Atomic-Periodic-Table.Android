package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.AEquationBalancerBinding
import com.mckimquyen.atomicPeriodicTable.util.ChemicalEquationBalancer
import com.mckimquyen.atomicPeriodicTable.util.Utils

class EquationBalancerAct : BaseAct() {

    private lateinit var binding: AEquationBalancerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AEquationBalancerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.commonTitleBack.layoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBack.layoutParams = params

        val topPadding = resources.getDimensionPixelSize(R.dimen.margin)
        val bottomPadding = bottom + resources.getDimensionPixelSize(R.dimen.margin)
        binding.balancerScrollView.setPadding(0, topPadding, 0, bottomPadding)
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.balancerBtn.setOnClickListener {
            performBalancing()
        }

        binding.balancerInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                performBalancing()
                true
            } else {
                false
            }
        }
    }

    private fun performBalancing() {
        // Clear previous state
        binding.balancerInputLayout.error = null
        binding.balancerResultCard.visibility = View.GONE

        val input = binding.balancerInput.text.toString().trim()
        if (input.isEmpty()) {
            return
        }

        // Validate basic format before solving
        if (!input.contains("=") && !input.contains("->")) {
            binding.balancerInputLayout.error = getString(R.string.balancer_error_invalid)
            return
        }

        val result = ChemicalEquationBalancer.balance(input)
        if (result != null) {
            binding.balancerResultText.text = result.balancedString
            binding.balancerResultCard.visibility = View.VISIBLE
            Utils.slideUpFadeIn(binding.balancerResultCard, 400)
        } else {
            // Determine if the issue is mismatched elements or solver failure
            val parts = input.split("=", "->")
            if (parts.size == 2) {
                try {
                    val rParsed = parts[0].split("+").map { it.trim() }.filter { it.isNotEmpty() }.flatMap { com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser.parse(it).keys }.toSet()
                    val pParsed = parts[1].split("+").map { it.trim() }.filter { it.isNotEmpty() }.flatMap { com.mckimquyen.atomicPeriodicTable.util.ChemicalFormulaParser.parse(it).keys }.toSet()
                    if (rParsed != pParsed) {
                        binding.balancerInputLayout.error = getString(R.string.balancer_error_elements)
                        return
                    }
                } catch (e: Exception) {
                    binding.balancerInputLayout.error = getString(R.string.molar_mass_error)
                    return
                }
            }
            binding.balancerInputLayout.error = getString(R.string.balancer_error_unbalanced)
        }
    }
}
