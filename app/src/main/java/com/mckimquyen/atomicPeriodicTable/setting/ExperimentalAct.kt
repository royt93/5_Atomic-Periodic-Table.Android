package com.mckimquyen.atomicPeriodicTable.setting

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.AExperimentalSettingsPageBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class ExperimentalAct : BaseAct() {

    private lateinit var binding: AExperimentalSettingsPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()
        if (themePrefValue == 0) {
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            setTheme(R.style.AppThemeDark)
        }
        binding = AExperimentalSettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //onClickListeners() //Disabled as a result of conflicts between ACTION_DOWN and ScrollView
        binding.viewe.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        binding.backBtnExp.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val params = binding.commonTitleBackExp.layoutParams as ViewGroup.LayoutParams
        params.height += top
        binding.commonTitleBackExp.layoutParams = params

        val params2 = binding.generalHeaderExp.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin += top
        binding.generalHeaderExp.layoutParams = params2
    }

}
