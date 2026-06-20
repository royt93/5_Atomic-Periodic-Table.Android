package com.mckimquyen.atomicPeriodicTable.act

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.Display
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.mckimquyen.atomicPeriodicTable.util.LocaleHelper

abstract class BaseAct : AppCompatActivity(), View.OnApplyWindowInsetsListener {
    companion object {
        private const val TAG = "BaseActivity"
    }

    private var systemUiConfigured = false

    // Subclasses that manage their own window theme (e.g. SplashAct with SplashTheme)
    // can return false to skip the app-wide theme override.
    protected open fun shouldApplyTheme(): Boolean = true

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        if (shouldApplyTheme()) applyTheme()
        super.onCreate(savedInstanceState)
    }

    private fun applyTheme() {
        val themePref = com.mckimquyen.atomicPeriodicTable.pref.ThemePref(this)
        when (themePref.getValue()) {
            100 -> when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> setTheme(com.mckimquyen.atomicPeriodicTable.R.style.AppTheme)
                Configuration.UI_MODE_NIGHT_YES -> setTheme(com.mckimquyen.atomicPeriodicTable.R.style.AppThemeDark)
            }
            0 -> setTheme(com.mckimquyen.atomicPeriodicTable.R.style.AppTheme)
            1 -> setTheme(com.mckimquyen.atomicPeriodicTable.R.style.AppThemeDark)
        }
    }

    override fun attachBaseContext(context: Context) {
        // Apply the saved language and font scale via LocaleHelper
        val localizedContext = LocaleHelper.applyLanguage(context)
        super.attachBaseContext(localizedContext)
    }

    override fun onStart() {
        super.onStart()
        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener(this)

        if (!systemUiConfigured) {
            systemUiConfigured = true
        }
    }

    open fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) = Unit

    // ===============================================================
    // Window Insets Handler (Modern API - Android 11+)
    // ===============================================================
    // Thay thế: WindowInsets với systemWindowInsetTop/Bottom/Left/Right (deprecated)
    // Sử dụng: WindowInsetsCompat với getInsets() (modern, backward compatible)

    @Suppress("DEPRECATION")
    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        // Convert platform WindowInsets to WindowInsetsCompat for modern API
        val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets, v)

        // Lấy system bars insets (status bar, navigation bar) bằng modern API
        val systemBarsInsets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())

        // Gọi callback với insets values
        onApplySystemInsets(
            systemBarsInsets.top,
            systemBarsInsets.bottom,
            systemBarsInsets.left,
            systemBarsInsets.right
        )

        // Return consumed insets using modern API
        return WindowInsetsCompat.CONSUMED.toWindowInsets() ?: insets
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableAdaptiveRefreshRate()
        }
    }

    private fun enableAdaptiveRefreshRate() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display // Sử dụng API mới
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay // Fallback cho API thấp hơn
        }


        if (display != null) {
            val supportedModes = display.supportedModes
            val highestRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }

            if (highestRefreshRateMode != null) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = highestRefreshRateMode.modeId
                }
                println("Adaptive refresh rate applied: ${highestRefreshRateMode.refreshRate} Hz")
            }
        }
    }
}
