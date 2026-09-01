package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.AUnitConverterBinding
import com.mckimquyen.atomicPeriodicTable.feature.converter.MassUnit
import com.mckimquyen.atomicPeriodicTable.feature.converter.PressureUnit
import com.mckimquyen.atomicPeriodicTable.feature.converter.UnitConverter
import com.mckimquyen.atomicPeriodicTable.feature.converter.VolumeUnit

class UnitConverterAct : BaseAct() {

    private lateinit var binding: AUnitConverterBinding

    private enum class Category { PRESSURE, MASS, VOLUME }

    private var category = Category.PRESSURE
    private var fromIndex = 0
    private var toIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding = AUnitConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            category = when (checkedIds.firstOrNull()) {
                binding.chipMass.id -> Category.MASS
                binding.chipVolume.id -> Category.VOLUME
                else -> Category.PRESSURE
            }
            fromIndex = 0
            toIndex = 1
            populateDropdowns()
            recompute()
        }

        binding.editConverterValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = recompute()
        })

        binding.dropdownConverterFrom.setOnItemClickListener { _, _, position, _ ->
            fromIndex = position
            recompute()
        }
        binding.dropdownConverterTo.setOnItemClickListener { _, _, position, _ ->
            toIndex = position
            recompute()
        }

        binding.btnConverterSwap.setOnClickListener {
            val temp = fromIndex
            fromIndex = toIndex
            toIndex = temp
            populateDropdowns()
            recompute()
        }

        populateDropdowns()
        recompute()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        binding.converterBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun unitLabels(): List<String> = when (category) {
        Category.PRESSURE -> PressureUnit.entries.map { it.label }
        Category.MASS -> MassUnit.entries.map { it.label }
        Category.VOLUME -> VolumeUnit.entries.map { it.label }
    }

    private fun populateDropdowns() {
        val labels = unitLabels()
        // Material's own dropdown-item layout (not android.R.layout.simple_list_item_1) so the
        // popup respects the app's theme colors instead of hardcoded black-on-white.
        val itemLayout = androidx.appcompat.R.layout.support_simple_spinner_dropdown_item
        binding.dropdownConverterFrom.setAdapter(ArrayAdapter(this, itemLayout, labels))
        binding.dropdownConverterTo.setAdapter(ArrayAdapter(this, itemLayout, labels))
        binding.dropdownConverterFrom.setText(labels[fromIndex], false)
        binding.dropdownConverterTo.setText(labels[toIndex], false)
    }

    private fun recompute() {
        val rawValue = binding.editConverterValue.text?.toString()?.toDoubleOrNull()
        binding.layoutConverterInput.error = null
        if (rawValue == null) {
            binding.tvConverterResult.text = ""
            return
        }

        val labels = unitLabels()
        val result = try {
            when (category) {
                Category.PRESSURE -> UnitConverter.convertPressure(
                    rawValue, PressureUnit.entries[fromIndex], PressureUnit.entries[toIndex],
                )
                Category.MASS -> UnitConverter.convertMass(
                    rawValue, MassUnit.entries[fromIndex], MassUnit.entries[toIndex],
                )
                Category.VOLUME -> UnitConverter.convertVolume(
                    rawValue, VolumeUnit.entries[fromIndex], VolumeUnit.entries[toIndex],
                )
            }
        } catch (e: IllegalArgumentException) {
            binding.layoutConverterInput.error = getString(R.string.unit_converter_negative_error)
            binding.tvConverterResult.text = ""
            return
        }

        binding.tvConverterResult.text = getString(
            R.string.unit_converter_result_format,
            rawValue,
            labels[fromIndex],
            result,
            labels[toIndex],
        )
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.converterTitleBar.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.converterTitleBar.layoutParams = params
        binding.converterRootView.setPadding(0, 0, 0, bottom)
    }
}
