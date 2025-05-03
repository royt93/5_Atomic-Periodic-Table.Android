package com.mckimquyen.atomicPeriodicTable.act

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.setting.AboutAct
import com.mckimquyen.atomicPeriodicTable.act.setting.FavoritePageAct
import com.mckimquyen.atomicPeriodicTable.act.setting.LicensesAct
import com.mckimquyen.atomicPeriodicTable.act.setting.OrderAct
import com.mckimquyen.atomicPeriodicTable.act.setting.SubmitAct
import com.mckimquyen.atomicPeriodicTable.act.setting.UnitAct
import com.mckimquyen.atomicPeriodicTable.ext.rateApp
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.setting.ExperimentalAct
import com.mckimquyen.atomicPeriodicTable.util.Utils
import kotlinx.android.synthetic.main.a_settings.aboutSettings
import kotlinx.android.synthetic.main.a_settings.advancedBox
import kotlinx.android.synthetic.main.a_settings.backBtnSetting
import kotlinx.android.synthetic.main.a_settings.cacheLay
import kotlinx.android.synthetic.main.a_settings.commonTitleBackSet
import kotlinx.android.synthetic.main.a_settings.commonTitleSettingsColor
import kotlinx.android.synthetic.main.a_settings.elementTitle
import kotlinx.android.synthetic.main.a_settings.elementTitleDownstate
import kotlinx.android.synthetic.main.a_settings.experimentalSettings
import kotlinx.android.synthetic.main.a_settings.favoriteSettings
import kotlinx.android.synthetic.main.a_settings.flAd
import kotlinx.android.synthetic.main.a_settings.github20TesterSettings
import kotlinx.android.synthetic.main.a_settings.licensesSettings
import kotlinx.android.synthetic.main.a_settings.offlineInternetSwitch
import kotlinx.android.synthetic.main.a_settings.offlineSettings
import kotlinx.android.synthetic.main.a_settings.orderSettings
import kotlinx.android.synthetic.main.a_settings.personalizationBox
import kotlinx.android.synthetic.main.a_settings.scrollSettings
import kotlinx.android.synthetic.main.a_settings.submitSettings
import kotlinx.android.synthetic.main.a_settings.themePanel
import kotlinx.android.synthetic.main.a_settings.themesSettings
import kotlinx.android.synthetic.main.a_settings.titleBoxSettings
import kotlinx.android.synthetic.main.a_settings.unitSettings
import kotlinx.android.synthetic.main.a_settings.view
import kotlinx.android.synthetic.main.view_theme_panel.cancelBtn
import kotlinx.android.synthetic.main.view_theme_panel.darkBtn
import kotlinx.android.synthetic.main.view_theme_panel.lightBtn
import kotlinx.android.synthetic.main.view_theme_panel.systemDefaultBtn
import kotlinx.android.synthetic.main.view_theme_panel.themeBackground
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import kotlin.system.exitProcess

class SettingsAct : BaseAct() {

    //TODO roy93~ admob banner
//    private var adView: MaxAdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    override fun onDestroy() {
        //TODO roy93~ admob banner
//        flAd.destroyAdBanner(adView)
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
        setContentView(R.layout.a_settings)

        if (themePrefValue == 100) {
            systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_checked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            lightBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 0) {
            systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            lightBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
            darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 1) {
            systemDefaultBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            lightBtn.setCompoundDrawablesWithIntrinsicBounds(
                /* left = */ R.drawable.ic_radio_unchecked,
                /* top = */ 0,
                /* right = */ 0,
                /* bottom = */ 0
            )
            darkBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
        }

        openPages()
        themeSettings()
        initializeCache()
        cacheSettings()
        initOfflineSwitches()

        offlineSettings.setOnClickListener {
            offlineInternetSwitch.toggle()
        }

        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        //Title Controller
        commonTitleSettingsColor.visibility = View.INVISIBLE
        elementTitle.visibility = View.INVISIBLE
        commonTitleBackSet.elevation = (resources.getDimension(R.dimen.zero_elevation))
        scrollSettings.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (scrollSettings.scrollY > 150) {
                        commonTitleSettingsColor.visibility = View.VISIBLE
                        elementTitle.visibility = View.VISIBLE
                        elementTitleDownstate.visibility = View.INVISIBLE
                        commonTitleBackSet.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        commonTitleSettingsColor.visibility = View.INVISIBLE
                        elementTitle.visibility = View.INVISIBLE
                        elementTitleDownstate.visibility = View.VISIBLE
                        commonTitleBackSet.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = scrollSettings.scrollY.toFloat()
                }
            })

        aboutSettings.setOnClickListener {
            val intent = Intent(this, AboutAct::class.java)
            startActivity(intent)
        }
        github20TesterSettings.setOnClickListener {
            rateApp("com.mckimquyen.bemytester")
//            openUrlInBrowser("https://github.com/gj-loitp/20-TESTER-FOR-CLOSED-TESTING/tree/main")
        }
        backBtnSetting.setOnClickListener {
            Log.d("roy93~", "onBackPressed")
            this.onBackPressed()
//            finishScreen()
        }
        submitSettings.setOnClickListener {
            val intent = Intent(this, SubmitAct::class.java)
            startActivity(intent)
        }
        licensesSettings.setOnClickListener {
            val intent = Intent(this, LicensesAct::class.java)
            startActivity(intent)
        }
        unitSettings.setOnClickListener {
            val intent = Intent(this, UnitAct::class.java)
            startActivity(intent)
        }

        //TODO roy93~ admob banner
//        adView = this.createAdBanner(
//            logTag = SettingsAct::class.simpleName,
//            viewGroup = flAd,
//            isAdaptiveBanner = true,
//        )
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val params = commonTitleBackSet.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        commonTitleBackSet.layoutParams = params

        val titleParam = titleBoxSettings.layoutParams as ViewGroup.MarginLayoutParams
        titleParam.rightMargin = right
        titleParam.leftMargin = left
        titleBoxSettings.layoutParams = titleParam

        val params2 = elementTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        elementTitleDownstate.layoutParams = params2

        personalizationBox.setPadding(left, 0, right, 0)
        advancedBox.setPadding(left, 0, right, 0)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d("roy93~", "onBackPressed")
        if (themePanel.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(themePanel, 300) //Start Close Animation
            return
        } else {
            super.onBackPressed()
        }
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
        offlineInternetSwitch.isChecked = offlinePrefValue == 1

        offlineInternetSwitch.setOnCheckedChangeListener { _, _ ->
            if (offlineInternetSwitch.isChecked) {
                val offlinePreference = OfflinePreference(this)
                offlinePreference.setValue(1)
            } else {
                val offlinePreference = OfflinePreference(this)
                offlinePreference.setValue(0)
            }
        }
    }

    private fun openPages() {
        favoriteSettings.setOnClickListener {
            val intent = Intent(this, FavoritePageAct::class.java)
            startActivity(intent)
        }
        orderSettings.setOnClickListener {
            val intent = Intent(this, OrderAct::class.java)
            startActivity(intent)
        }
        experimentalSettings.setOnClickListener {
            val intent = Intent(this, ExperimentalAct::class.java)
            startActivity(intent)
        }
    }

    private fun cacheSettings() {
        cacheLay.setOnClickListener {
            this.cacheDir.deleteRecursively()
            initializeCache()
        }
    }

    private fun initializeCache() {
        var size: Long = 0
        size += getDirSize(this.cacheDir)
        size += getDirSize(this.externalCacheDir)
        (findViewById<View>(R.id.clearCacheContent) as TextView).text = readableFileSize(size)
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
        systemDefaultBtn.setOnClickListener {
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
            Utils.fadeOutAnim(themePanel, 300)
            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        lightBtn.setOnClickListener {
            val themePref = ThemePref(this)
            themePref.setValue(0)
            setTheme(R.style.AppTheme)
            Utils.fadeOutAnim(themePanel, 300)

            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        darkBtn.setOnClickListener {
            val themePref = ThemePref(this)
            themePref.setValue(1)
            setTheme(R.style.AppThemeDark)
            Utils.fadeOutAnim(themePanel, 300)

            val delayChange = Handler(Looper.getMainLooper())
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsAct().finish()
                exitProcess(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        themesSettings.setOnClickListener {
            Utils.fadeInAnim(view = themePanel, time = 300)
        }
        themeBackground.setOnClickListener {
            Utils.fadeOutAnim(themePanel, 300)
        }
        cancelBtn.setOnClickListener {
            Utils.fadeOutAnim(themePanel, 300)
        }
    }
}
