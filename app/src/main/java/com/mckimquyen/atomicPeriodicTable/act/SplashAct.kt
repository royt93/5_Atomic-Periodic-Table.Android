package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.RoyApp
import com.roy.sdkadbmob.AdManager

@SuppressLint("CustomSplashScreen")
class SplashAct : AppCompatActivity() {

    // Memory leak prevention: Store view references and animators for cleanup
    private var logoCard: android.view.View? = null
    private var appNameText: android.view.View? = null
    private var loadingText: android.view.View? = null
    private var progressBar: android.view.View? = null
    private var versionText: android.view.View? = null
    private var decorCircle1: android.view.View? = null
    private var decorCircle2: android.view.View? = null

    // Offline safety: if ad init doesn't complete within 8s (no network), proceed anyway.
    private val splashTimeoutHandler = Handler(Looper.getMainLooper())
    private var navigatedToMain = false
    private val splashTimeoutRunnable = Runnable {
        goToMain()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Set version text dynamically
        findViewById<android.widget.TextView>(R.id.versionText)?.text =
            "Version ${com.mckimquyen.atomicPeriodicTable.BuildConfig.VERSION_NAME}"

        // Animate splash screen elements
        animateSplashScreen()

        if (hasNetworkConnectivity()) {
            // Online: consent flow với 3s fallback (UMP callback trong < 1s trên mọi thiết bị)
            splashTimeoutHandler.postDelayed(splashTimeoutRunnable, 3_000L)
            AdManager.requestConsentInfoUpdate(this) {
                splashTimeoutHandler.removeCallbacks(splashTimeoutRunnable)
                (application as RoyApp).initializeAdsIfNeeded { _, _ ->
                    AdManager.initSplashScreen(this) { goToMain() }
                }
            }
        } else {
            // Offline: bỏ qua consent, vào app ngay lập tức
            (application as RoyApp).initializeAdsIfNeeded { _, _ ->
                AdManager.initSplashScreen(this) { goToMain() }
            }
        }
    }

    private fun animateSplashScreen() {
        // Get views and store in member variables for cleanup
        logoCard = findViewById(R.id.logoCard)
        appNameText = findViewById(R.id.appNameText)
        loadingText = findViewById(R.id.loadingText)
        progressBar = findViewById(R.id.progressBar)
        versionText = findViewById(R.id.versionText)
        decorCircle1 = findViewById(R.id.decorCircle1)
        decorCircle2 = findViewById(R.id.decorCircle2)

        // Initial state - all invisible
        logoCard?.alpha = 0f
        logoCard?.scaleX = 0.7f
        logoCard?.scaleY = 0.7f
        appNameText?.alpha = 0f
        appNameText?.translationY = 20f
        loadingText?.alpha = 0f
        progressBar?.alpha = 0f
        versionText?.alpha = 0f

        // Animate logo card - scale and fade in
        logoCard?.animate()
            ?.alpha(1f)
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.setDuration(600)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()

        // Animate app name - fade in and slide up
        appNameText?.animate()
            ?.alpha(1f)
            ?.translationY(0f)
            ?.setStartDelay(200)
            ?.setDuration(500)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()

        // Animate loading text - fade in
        loadingText?.animate()
            ?.alpha(0.87f)
            ?.setStartDelay(400)
            ?.setDuration(400)
            ?.start()

        // Animate progress bar - fade in
        progressBar?.animate()
            ?.alpha(1f)
            ?.setStartDelay(600)
            ?.setDuration(400)
            ?.start()

        // Animate decorative circles - continuous subtle rotation
        // Memory leak fix: Check if activity is finishing before restarting animation
        decorCircle1?.animate()
            ?.rotation(360f)
            ?.setDuration(20000)
            ?.setInterpolator(android.view.animation.LinearInterpolator())
            ?.withEndAction {
                // Only repeat if activity is not finishing
                if (!isFinishing && decorCircle1 != null) {
                    decorCircle1?.rotation = 0f
                    decorCircle1?.animate()
                        ?.rotation(360f)
                        ?.setDuration(20000)
                        ?.setInterpolator(android.view.animation.LinearInterpolator())
                        ?.start()
                }
            }
            ?.start()

        decorCircle2?.animate()
            ?.rotation(-360f)
            ?.setDuration(25000)
            ?.setInterpolator(android.view.animation.LinearInterpolator())
            ?.withEndAction {
                // Only repeat if activity is not finishing
                if (!isFinishing && decorCircle2 != null) {
                    decorCircle2?.rotation = 0f
                    decorCircle2?.animate()
                        ?.rotation(-360f)
                        ?.setDuration(25000)
                        ?.setInterpolator(android.view.animation.LinearInterpolator())
                        ?.start()
                }
            }
            ?.start()

        // Animate version text - fade in
        versionText?.animate()
            ?.alpha(0.5f)
            ?.setStartDelay(800)
            ?.setDuration(400)
            ?.start()
    }

    private fun hasNetworkConnectivity(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
                ?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun goToMain() {
        if (navigatedToMain) return
        navigatedToMain = true
        splashTimeoutHandler.removeCallbacks(splashTimeoutRunnable)
        val intent = Intent(this, MainAct::class.java)
        startActivity(intent)
//        overridePendingTransition(0, 0)
//        finishAffinity()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // Trì hoãn finish để đợi animation hoàn tất
        window.decorView.postDelayed({
            finish() // Finish sau animation
        }, 300) // delay khoảng 300ms (hoặc đúng thời gian của animation)
    }

    override fun attachBaseContext(context: Context) {
        val override = Configuration(context.resources.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
        super.attachBaseContext(context)
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

    override fun onDestroy() {
        splashTimeoutHandler.removeCallbacks(splashTimeoutRunnable)
        // Memory leak fix: Cancel all animations to prevent holding Activity reference
        logoCard?.animate()?.cancel()
        appNameText?.animate()?.cancel()
        loadingText?.animate()?.cancel()
        progressBar?.animate()?.cancel()
        versionText?.animate()?.cancel()
        decorCircle1?.animate()?.cancel()
        decorCircle2?.animate()?.cancel()

        // Clear references
        logoCard = null
        appNameText = null
        loadingText = null
        progressBar = null
        versionText = null
        decorCircle1 = null
        decorCircle2 = null

        super.onDestroy()
    }
}
