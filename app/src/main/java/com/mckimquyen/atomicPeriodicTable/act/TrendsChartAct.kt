package com.mckimquyen.atomicPeriodicTable.act

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ATrendsChartBinding
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator

class TrendsChartAct : BaseAct() {

    private lateinit var binding: ATrendsChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding = ATrendsChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        binding.trendsChartView.setElements(elements)
        binding.trendsChartView.setOnPointSelectedListener { element ->
            val name = ElementTranslator.getLocalizedName(this, element.element)
            binding.tvTrendsTooltip.text =
                getString(R.string.trends_tooltip_format, element.short, name, element.electro)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        binding.trendsBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.trendsTitleBar.layoutParams as android.view.ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.trendsTitleBar.layoutParams = params
        binding.trendsRootView.setPadding(0, 0, 0, bottom)
    }
}
