package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.databinding.ASettingsLicensesBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class LicensesAct : BaseAct() {

    // ViewBinding - thay thế Kotlin Synthetics (deprecated)
    private lateinit var binding: ASettingsLicensesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

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
        binding = ASettingsLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup system UI visibility
        binding.viewLic.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // Title Controller - hiển thị/ẩn title bar khi scroll
        binding.commonTitleBackLicColor.visibility = View.INVISIBLE
        binding.licenseTitle.visibility = View.INVISIBLE
        binding.commonTitleBackLic.elevation = (resources.getDimension(R.dimen.zero_elevation))
        binding.licenseScroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (binding.licenseScroll.scrollY > 150) {
                        // Đã scroll xuống -> hiện title bar compact
                        binding.commonTitleBackLicColor.visibility = View.VISIBLE
                        binding.licenseTitle.visibility = View.VISIBLE
                        binding.licenseTitleDownstate.visibility = View.INVISIBLE
                        binding.commonTitleBackLic.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        // Ở đầu trang → hiện title bar expanded
                        binding.commonTitleBackLicColor.visibility = View.INVISIBLE
                        binding.licenseTitle.visibility = View.INVISIBLE
                        binding.licenseTitleDownstate.visibility = View.VISIBLE
                        binding.commonTitleBackLic.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = binding.licenseScroll.scrollY.toFloat()
                }
            })

        listeners()

        // Back button - quay về màn hình trước
        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        // Adjust title bar height để tránh status bar
        val params2 = binding.commonTitleBackLic.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackLic.layoutParams = params2

        // Adjust expanded title top margin
        val params3 = binding.licenseTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params3.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        binding.licenseTitleDownstate.layoutParams = params3
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Nếu license info panel đang hiển thị -> ẩn panel thay vì back
        if (binding.lInc.root.visibility == View.VISIBLE) {
            hideInfoPanel()
            return
        } else {
            super.onBackPressed()
        }
    }

    private fun listeners() {
        // Wikipedia license button
        binding.lWikiBtn.setOnClickListener {
            val title = resources.getString(R.string.wikipedia_license)
            val text = resources.getString(R.string.wikipedia_license_text)
            showInfoPanel(title, text)
        }

        // Sothree (SlidingUpPanel) license button
        binding.lSothreeBtn.setOnClickListener {
            val title = resources.getString(R.string.sothree_license)
            val text = resources.getString(R.string.sothree_license_text)
            showInfoPanel(title, text)
        }

        // Close license info panel buttons
        binding.lInc.lBackBtn.setOnClickListener { hideInfoPanel() }
        binding.lInc.lBackground3.setOnClickListener { hideInfoPanel() }
    }

    private fun showInfoPanel(title: String, text: String) {
        // Hiển thị license info panel với animation fade in
        Anim.fadeIn(binding.lInc.root, 150)

        // Set nội dung license
        binding.lInc.lTitle.text = title
        binding.lInc.lText.text = text
    }

    private fun hideInfoPanel() {
        // Ẩn license info panel với animation fade out
        Anim.fadeOutAnim(view = binding.lInc.root, time = 150)
    }
}
