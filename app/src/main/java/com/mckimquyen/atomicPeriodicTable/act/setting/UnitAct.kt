package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.AUnitBinding
import com.mckimquyen.atomicPeriodicTable.pref.TemperatureUnits
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class UnitAct : BaseAct() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: AUnitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    setTheme(R.style.AppTheme)
                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    setTheme(R.style.AppThemeDark)
                }
            }
        }
        if (themePrefValue == 0) {
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            setTheme(R.style.AppThemeDark)
        }

        // Khởi tạo ViewBinding
        binding = AUnitBinding.inflate(layoutInflater)
        setContentView(binding.root) //REMEMBER: Never move any function calls above this

        binding.viewUnit.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        //Title Controller
        binding.commonTitleBackUnitColor.visibility = View.INVISIBLE
        binding.unitTitle.visibility = View.INVISIBLE
        binding.commonTitleBackunit.elevation = (resources.getDimension(R.dimen.zero_elevation))
        binding.unitScroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (binding.unitScroll.scrollY > 150) {
                        binding.commonTitleBackUnitColor.visibility = View.VISIBLE
                        binding.unitTitle.visibility = View.VISIBLE
                        binding.unitTitleDownstate.visibility = View.INVISIBLE
                        binding.commonTitleBackunit.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        binding.commonTitleBackUnitColor.visibility = View.INVISIBLE
                        binding.unitTitle.visibility = View.INVISIBLE
                        binding.unitTitleDownstate.visibility = View.VISIBLE
                        binding.commonTitleBackunit.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = binding.unitScroll.scrollY.toFloat()
                }
            })

        tempUnits()

        binding.backBtnUnit.setOnClickListener {
            this.onBackPressed()
        }
    }

    private fun tempUnits() {
        val tempPreference = TemperatureUnits(this)
        val tempPrefValue = tempPreference.getValue()
        if (tempPrefValue == "kelvin") {
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        if (tempPrefValue == "celsius") {
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        if (tempPrefValue == "fahrenheit") {
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
        }
        binding.kelvinBtn.setOnClickListener {
            tempPreference.setValue("kelvin")
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        binding.celsiusBtn.setOnClickListener {
            tempPreference.setValue("celsius")
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        binding.fahrenheitbtn.setOnClickListener {
            tempPreference.setValue("fahrenheit")
            binding.kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            binding.fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val paramsTitle = binding.commonTitleBackunit.layoutParams as ViewGroup.LayoutParams
        paramsTitle.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackunit.layoutParams = paramsTitle

        val paramsLin = binding.unitTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        paramsLin.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        binding.unitTitleDownstate.layoutParams = paramsLin
    }
}
