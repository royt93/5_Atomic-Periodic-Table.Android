package com.mckimquyen.atomicPeriodicTable.act

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.setting.AboutAct
import com.mckimquyen.atomicPeriodicTable.act.setting.FavoritePageAct
import com.mckimquyen.atomicPeriodicTable.act.setting.LicensesAct
import com.mckimquyen.atomicPeriodicTable.act.setting.OrderAct
import com.mckimquyen.atomicPeriodicTable.act.setting.SubmitAct
import com.mckimquyen.atomicPeriodicTable.act.setting.UnitAct
import com.mckimquyen.atomicPeriodicTable.databinding.ASettingsBinding
import com.mckimquyen.atomicPeriodicTable.ext.rateApp
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.sdkadbmob.AdMobManager
import com.mckimquyen.atomicPeriodicTable.setting.ExperimentalAct
import com.mckimquyen.atomicPeriodicTable.util.Utils
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import kotlin.system.exitProcess
import androidx.core.view.isVisible

class SettingsAct : BaseAct() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: ASettingsBinding

    //    private var adView: MaxAdView? = null
    private var adView: AdView? = null

    // ===============================================================
    // Helper function: Modern Activity Transition API
    // ===============================================================
    // Thay thế: overridePendingTransition() (deprecated in Android 13+)
    // Sử dụng: overrideActivityTransition() (modern API for Android 13+)

    private fun Activity.overrideTransition(enterAnim: Int, exitAnim: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34+): Use modern overrideActivityTransition
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, exitAnim)
        } else {
            // Android 13 and below: Use deprecated overridePendingTransition
            @Suppress("DEPRECATION")
            overridePendingTransition(enterAnim, exitAnim)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
//        flAd.destroyAdBanner(adView)
        adView?.destroy()
        super.onDestroy()
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
        binding = ASettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (themePrefValue == 100) {
            binding.themePanel.systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_checked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            binding.themePanel.lightBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            binding.themePanel.darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 0) {
            binding.themePanel.systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            binding.themePanel.lightBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
            binding.themePanel.darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 1) {
            binding.themePanel.systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            binding.themePanel.lightBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            binding.themePanel.darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
        }

        openPages()
        themeSettings()
        initializeCache()
        cacheSettings()
        initOfflineSwitches()

        binding.offlineSettings.setOnClickListener {
            binding.offlineInternetSwitch.toggle()
        }

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

        //Title Controller
        binding.commonTitleSettingsColor.visibility = View.INVISIBLE
        binding.elementTitle.visibility = View.INVISIBLE
        binding.commonTitleBackSet.elevation = (resources.getDimension(R.dimen.zero_elevation))
        binding.scrollSettings.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (binding.scrollSettings.scrollY > 150) {
                        binding.commonTitleSettingsColor.visibility = View.VISIBLE
                        binding.elementTitle.visibility = View.VISIBLE
                        binding.elementTitleDownstate.visibility = View.INVISIBLE
                        binding.commonTitleBackSet.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        binding.commonTitleSettingsColor.visibility = View.INVISIBLE
                        binding.elementTitle.visibility = View.INVISIBLE
                        binding.elementTitleDownstate.visibility = View.VISIBLE
                        binding.commonTitleBackSet.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = binding.scrollSettings.scrollY.toFloat()
                }
            })

        binding.aboutSettings.setOnClickListener {
            val intent = Intent(this, AboutAct::class.java)
            startActivity(intent)
        }
        binding.github20TesterSettings.setOnClickListener {
            rateApp("com.mckimquyen.bemytester")
//            openUrlInBrowser("https://github.com/gj-loitp/20-TESTER-FOR-CLOSED-TESTING/tree/main")
        }

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press với logic đóng theme panel trước
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Nếu theme panel đang hiển thị, đóng nó lại
                if (binding.themePanel.root.isVisible) {
                    Utils.fadeOutAnim(binding.themePanel.root, 300) //Start Close Animation
                    return
                }
                // Không có panel nào mở, kết thúc activity
                finish()
            }
        })

        // Click listener cho nút back trên UI
        binding.backBtnSetting.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
        binding.submitSettings.setOnClickListener {
            val intent = Intent(this, SubmitAct::class.java)
            startActivity(intent)
        }
        binding.licensesSettings.setOnClickListener {
            val intent = Intent(this, LicensesAct::class.java)
            startActivity(intent)
        }
        binding.unitSettings.setOnClickListener {
            val intent = Intent(this, UnitAct::class.java)
            startActivity(intent)
        }

        val bannerContainer = findViewById<ViewGroup>(R.id.bannerContainer)
        val tvLabelAd = findViewById<TextView>(R.id.tvLabelAd)
        adView = AdMobManager.loadBanner(
            context = this,
            adUnitId = BuildConfig.ADMOB_BANNER_ID,
            container = bannerContainer,
            tvLabelAd = tvLabelAd,
            adSize = AdSize.LARGE_BANNER,
        )
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val params = binding.commonTitleBackSet.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackSet.layoutParams = params

        val titleParam = binding.titleBoxSettings.layoutParams as ViewGroup.MarginLayoutParams
        titleParam.rightMargin = right
        titleParam.leftMargin = left
        binding.titleBoxSettings.layoutParams = titleParam

        val params2 = binding.elementTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        binding.elementTitleDownstate.layoutParams = params2

        binding.personalizationBox.setPadding(left, 0, right, 0)
        binding.advancedBox.setPadding(left, 0, right, 0)

    }

//    private fun finishScreen() {
//        Log.d("roy93~", "onBackPressed")
//        if (themePanel.visibility == View.VISIBLE) {
//            Utils.fadeOutAnim(themePanel, 300) //Start Close Animation
//            return
//        } else {
//            super.onBackPressed()
//        }
//    }

    private fun initOfflineSwitches() {
        val offlinePreferences = OfflinePreference(this)
        val offlinePrefValue = offlinePreferences.getValue()
        binding.offlineInternetSwitch.isChecked = offlinePrefValue == 1

        binding.offlineInternetSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.offlineInternetSwitch.isChecked) {
                val offlinePreference = OfflinePreference(this)
                offlinePreference.setValue(1)
            } else {
                val offlinePreference = OfflinePreference(this)
                offlinePreference.setValue(0)
            }
        }
    }

    private fun openPages() {
        binding.favoriteSettings.setOnClickListener {
            val intent = Intent(this, FavoritePageAct::class.java)
            startActivity(intent)
        }
        binding.orderSettings.setOnClickListener {
            val intent = Intent(this, OrderAct::class.java)
            startActivity(intent)
        }
        binding.experimentalSettings.setOnClickListener {
            val intent = Intent(this, ExperimentalAct::class.java)
            startActivity(intent)
        }
    }

    private fun cacheSettings() {
        binding.cacheLay.setOnClickListener {
            this.cacheDir.deleteRecursively()
            initializeCache()
        }
    }

    private fun initializeCache() {
        var size: Long = 0
        size += getDirSize(this.cacheDir)
        size += getDirSize(this.externalCacheDir)
        // Use findViewById<TextView> directly - no cast needed
        findViewById<TextView>(R.id.clearCacheContent).text = readableFileSize(size)
    }

    private fun getDirSize(dir: File?): Long {
        var size: Long = 0
        dir?.listFiles()?.let {
            for (file in it) {
                if (file != null && file.isDirectory) {
                    size += getDirSize(file)
                } else if (file != null && file.isFile) {
                    size += file.length()
                }
            }
        }
        return size
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }

    private fun themeSettings() {
        binding.themePanel.systemDefaultBtn.setOnClickListener {
            val themePref = ThemePref(this)
            themePref.setValue(100)
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    setTheme(R.style.AppTheme)
                } // Night mode is not active, we're using the light theme
                Configuration.UI_MODE_NIGHT_YES -> {
                    setTheme(R.style.AppThemeDark)
                } // Night mode is active, we're using dark theme
            }
            Utils.fadeOutAnim(binding.themePanel.root, 300)
            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overrideTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overrideTransition(0, 0)
            }, 302)
        }
        binding.themePanel.lightBtn.setOnClickListener {
            val themePref = ThemePref(this)
            themePref.setValue(0)
            setTheme(R.style.AppTheme)
            Utils.fadeOutAnim(binding.themePanel.root, 300)

            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overrideTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overrideTransition(0, 0)
            }, 302)
        }
        binding.themePanel.darkBtn.setOnClickListener {
            val themePref = ThemePref(this)
            themePref.setValue(1)
            setTheme(R.style.AppThemeDark)
            Utils.fadeOutAnim(binding.themePanel.root, 300)

            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overrideTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overrideTransition(0, 0)
            }, 302)
        }
        binding.themesSettings.setOnClickListener {
            Utils.fadeInAnim(view = binding.themePanel.root, time = 300)
        }
        binding.themePanel.themeBackground.setOnClickListener {
            Utils.fadeOutAnim(binding.themePanel.root, 300)
        }
        binding.themePanel.cancelBtn.setOnClickListener {
            Utils.fadeOutAnim(binding.themePanel.root, 300)
        }
    }
}
