package com.mckimquyen.atomicPeriodicTable.act.setting

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.AInfoBinding
import com.mckimquyen.atomicPeriodicTable.ext.openUrlInBrowser
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class AboutAct : BaseAct() {

    // ViewBinding - thay thế Kotlin Synthetics (deprecated)
    private lateinit var binding: AInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        // Apply theme dựa trên user preference
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()

        if (themePrefValue == 100) {
            // Theme tự động theo system
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
            // Light theme
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            // Dark theme
            setTheme(R.style.AppThemeDark)
        }

        // Inflate ViewBinding và set content view
        binding = AInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup system UI visibility
        binding.viewInfo.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // Hiển thị version number từ BuildConfig
        binding.versionNumber.text = "Version ${BuildConfig.VERSION_NAME}"

        // Back button - quay về màn hình trước
        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }

        // Link tới GitHub repo forked
        binding.btGithubForked.setOnClickListener {
            openUrlInBrowser("https://github.com/gj-loitp/Atomic-Periodic-Table.Android")
        }

        // Link tới GitHub repo gốc
        binding.btGithub.setOnClickListener {
            openUrlInBrowser("https://github.com/JLindemann42/Atomic-Periodic-Table.Android")
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        // Adjust title bar height để tránh system bars (status bar)
        val params = binding.commonTitleBackInfo.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackInfo.layoutParams = params

        // Adjust image top margin để tránh status bar
        val params2 = binding.imageView3.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin += top
        binding.imageView3.layoutParams = params2

        // Adjust title box margins để tránh navigation bar (left/right)
        val titleParam = binding.titleBoxInfo.layoutParams as ViewGroup.MarginLayoutParams
        titleParam.rightMargin = right
        titleParam.leftMargin = left
        binding.titleBoxInfo.layoutParams = titleParam
    }
}
