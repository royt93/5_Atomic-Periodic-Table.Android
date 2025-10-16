package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.databinding.ASettingsLicensesBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import androidx.core.view.isVisible

class LicensesAct : BaseAct() {

    // ViewBinding - thay thế Kotlin Synthetics (deprecated)
    private lateinit var binding: ASettingsLicensesBinding

    // Memory leak prevention: Store listener for cleanup
    private var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = null

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

        // Title Controller - hiển thị/ẩn title bar khi scroll
        binding.commonTitleBackLicColor.visibility = View.INVISIBLE
        binding.licenseTitle.visibility = View.INVISIBLE
        binding.commonTitleBackLic.elevation = (resources.getDimension(R.dimen.zero_elevation))

        // Memory leak fix: Store listener for cleanup
        scrollChangedListener = object : ViewTreeObserver.OnScrollChangedListener {
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
        }
        binding.licenseScroll.viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)

        listeners()

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press
        // Logic đặc biệt: Nếu license info panel đang hiển thị -> ẩn panel thay vì back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kiểm tra xem license info panel có đang hiển thị không
                if (binding.lInc.root.isVisible) {
                    // Panel đang hiện -> ẩn panel thay vì finish activity
                    hideInfoPanel()
                } else {
                    // Panel đã ẩn -> finish activity và quay về màn hình trước
                    finish()
                }
            }
        })

        // Click listener cho nút back trên UI
        binding.backBtn.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
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

    override fun onDestroy() {
        // Memory leak fix: Clean up listener
        scrollChangedListener?.let {
            binding.licenseScroll.viewTreeObserver.removeOnScrollChangedListener(it)
        }
        scrollChangedListener = null
        super.onDestroy()
    }
}
