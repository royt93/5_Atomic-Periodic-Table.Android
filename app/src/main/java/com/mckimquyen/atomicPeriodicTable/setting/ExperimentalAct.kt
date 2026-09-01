package com.mckimquyen.atomicPeriodicTable.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.AExperimentalSettingsPageBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class ExperimentalAct : BaseAct() {

    private lateinit var binding: AExperimentalSettingsPageBinding

    // FIX-022: onApplySystemInsets can fire more than once (rotation, keyboard, system bar
    // visibility change) with the same absolute `top`. Accumulating with `+=` on top of a
    // value that already includes a previous inset made the header grow every time it fired.
    // Cache the original layout values once and always recompute from them.
    private var baseTitleBarHeight = -1
    private var baseHeaderTopMargin = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()

        // Optimized: Use when expression for mutually exclusive conditions
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme)
                Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.AppThemeDark)
            }
        }
        if (themePrefValue == 0) {
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            setTheme(R.style.AppThemeDark)
        }

        binding = AExperimentalSettingsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===============================================================
        // Setup Edge-to-Edge & System Bars (Modern API - Android 11+)
        // ===============================================================
        // Thay thế: systemUiVisibility (deprecated)
        // Sử dụng: WindowCompat.setDecorFitsSystemWindows (modern, backward compatible)
        //
        // Logic gốc: SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        // - Content vẽ DƯỚI system bars (status bar & navigation bar)
        // - Navigation bar KHÔNG bị ẩn, vẫn hiển thị bình thường
        //
        // Modern equivalent: chỉ cần setDecorFitsSystemWindows(false)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kết thúc activity và quay về màn hình trước
                finish()
            }
        })

        // Click listener cho nút back trên UI
        binding.backBtnExp.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val params = binding.commonTitleBackExp.layoutParams as ViewGroup.LayoutParams
        if (baseTitleBarHeight < 0) baseTitleBarHeight = params.height
        params.height = baseTitleBarHeight + top
        binding.commonTitleBackExp.layoutParams = params

        val params2 = binding.generalHeaderExp.layoutParams as ViewGroup.MarginLayoutParams
        if (baseHeaderTopMargin < 0) baseHeaderTopMargin = params2.topMargin
        params2.topMargin = baseHeaderTopMargin + top
        binding.generalHeaderExp.layoutParams = params2
    }

}
