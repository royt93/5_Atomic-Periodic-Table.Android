package com.mckimquyen.atomicPeriodicTable.act

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.databinding.ASolubilityBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class SolubilityAct : BaseAct() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: ASolubilityBinding

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
        binding = ASolubilityBinding.inflate(layoutInflater)
        setContentView(binding.root) //Don't move down (Needs to be before we call our functions)

        binding.viewSub.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        infoPanel()

        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val paramsO = binding.boxm.layoutParams as ViewGroup.MarginLayoutParams
        paramsO.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.boxm.layoutParams = paramsO

        val params2 = binding.commonTitleBackSul.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackSul.layoutParams = params2

    }

    private fun infoPanel() {
        binding.infoBtn.setOnClickListener {
            Anim.fadeIn(binding.infoPanel.root, 300)
            binding.infoPanel.infoTitle.text = resources.getString(R.string.solubility_info_t)
            binding.infoPanel.tvInfoText.text = resources.getString(R.string.solubility_info_c)
        }
        binding.infoPanel.infoBackBtn.setOnClickListener {
            Anim.fadeOutAnim(binding.infoPanel.root, 300)
        }
        binding.infoPanel.infoBackground.setOnClickListener {
            Anim.fadeOutAnim(binding.infoPanel.root, 300)
        }
    }
}
