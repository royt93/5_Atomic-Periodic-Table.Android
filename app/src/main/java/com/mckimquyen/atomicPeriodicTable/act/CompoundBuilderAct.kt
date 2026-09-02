package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import com.google.android.material.chip.Chip
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ACompoundBuilderBinding
import com.mckimquyen.atomicPeriodicTable.feature.compound.CompoundMatcher
import com.mckimquyen.atomicPeriodicTable.feature.compound.CompoundNames
import com.mckimquyen.atomicPeriodicTable.feature.exam.MolarMassQuestionGenerator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache

/**
 * Discovery UX distinct from CalculatorAct's free-text formula input: tap element chips to
 * build a compound, checked live against the same curated list Practice Exam uses
 * (MolarMassQuestionGenerator.COMMON_FORMULAS) — no attempt at real valence/bonding inference,
 * intentionally limited to "is this exact combination in the curated list or not".
 */
class CompoundBuilderAct : BaseAct() {

    private lateinit var binding: ACompoundBuilderBinding
    private val selection = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ACompoundBuilderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ElementWeightCache.init(this)
        binding.compoundBackBtn.setOnClickListener { finish() }
        binding.btnClearSelection.setOnClickListener {
            selection.clear()
            renderState()
        }

        for (symbol in CompoundMatcher.availableSymbols) {
            val chip = Chip(this, null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Action).apply {
                text = symbol
                isCheckable = false
                setOnClickListener {
                    selection.add(symbol)
                    renderState()
                }
            }
            binding.chipGroupElements.addView(chip)
        }

        renderState()
    }

    private fun renderState() {
        binding.tvSelectionPreview.text = if (selection.isEmpty()) {
            getString(R.string.compound_builder_empty_selection)
        } else {
            selection.groupingBy { it }.eachCount().entries.joinToString("  ") { (symbol, count) -> "$symbol×$count" }
        }

        val match = CompoundMatcher.findMatch(selection)
        when {
            match != null -> {
                val nameRes = CompoundNames.nameResFor(match)
                val name = if (nameRes != null) getString(nameRes) else match
                val mass = MolarMassQuestionGenerator.computeMolarMass(match) { symbol -> ElementWeightCache.getMass(symbol) } ?: 0.0
                binding.tvResult.text = getString(R.string.compound_builder_found_format, name, match, mass)
                binding.cardResult.visibility = View.VISIBLE
            }
            selection.isNotEmpty() -> {
                binding.tvResult.text = getString(R.string.compound_builder_not_found)
                binding.cardResult.visibility = View.VISIBLE
            }
            else -> {
                binding.cardResult.visibility = View.GONE
            }
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.compoundTitleBar.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.compoundTitleBar.layoutParams = params
        binding.compoundRootView.setPadding(0, 0, 0, bottom)
    }
}
