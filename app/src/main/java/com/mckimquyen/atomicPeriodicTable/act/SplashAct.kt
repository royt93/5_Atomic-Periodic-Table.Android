package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.RoyApp
import com.roy.sdkadbmob.AdManager

@SuppressLint("CustomSplashScreen")
class SplashAct : BaseAct() {

    private var logoCard: android.view.View? = null
    private var appNameText: android.view.View? = null
    private var loadingText: android.view.View? = null
    private var progressBar: android.view.View? = null
    private var versionText: android.view.View? = null
    private var decorCircle1: android.view.View? = null
    private var decorCircle2: android.view.View? = null

    private val splashTimeoutHandler = Handler(Looper.getMainLooper())
    private var navigatedToMain = false
    private val splashTimeoutRunnable = Runnable { goToMain() }
    private var finishRunnable: Runnable? = null
    // F1: store dismiss-listener reference so onDestroy can deregister it from RoyApp,
    // preventing the lambda from firing on a destroyed Activity.
    private var adDismissListener: (() -> Unit)? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<android.widget.TextView>(R.id.versionText)?.text =
            "Version ${com.mckimquyen.atomicPeriodicTable.BuildConfig.VERSION_NAME}"

        animateSplashScreen()

        // Bug 2: always call requestConsentInfoUpdate regardless of network.
        // UMP SDK uses cached consent when offline and returns immediately.
        // hasNetworkConnectivity() only adjusts the safety timeout, never skips consent.
        //
        // F3 — EEA consent debug: to test consent dialog on this Samsung device (SM-F731B),
        // pass testDeviceHashedId="8FD5578900ACE41E2AFF49D2497C0A2D" via AdSdkConfig when
        // the SDK exposes a consentTestDeviceIds field. Hash sourced from logcat UMP message.
        val timeoutMs = if (hasNetworkConnectivity()) 3_000L else 8_000L
        splashTimeoutHandler.postDelayed(splashTimeoutRunnable, timeoutMs)
        AdManager.requestConsentInfoUpdate(this) {
            splashTimeoutHandler.removeCallbacks(splashTimeoutRunnable)
            if (navigatedToMain) return@requestConsentInfoUpdate
            (application as RoyApp).initializeAdsIfNeeded { _, _ ->
                if (navigatedToMain) return@initializeAdsIfNeeded
                AdManager.initSplashScreen(this) { goToMain() }
            }
        }
    }

    private fun animateSplashScreen() {
        logoCard = findViewById(R.id.logoCard)
        appNameText = findViewById(R.id.appNameText)
        loadingText = findViewById(R.id.loadingText)
        progressBar = findViewById(R.id.progressBar)
        versionText = findViewById(R.id.versionText)
        decorCircle1 = findViewById(R.id.decorCircle1)
        decorCircle2 = findViewById(R.id.decorCircle2)

        logoCard?.alpha = 0f
        logoCard?.scaleX = 0.7f
        logoCard?.scaleY = 0.7f
        appNameText?.alpha = 0f
        appNameText?.translationY = 20f
        loadingText?.alpha = 0f
        progressBar?.alpha = 0f
        versionText?.alpha = 0f

        logoCard?.animate()
            ?.alpha(1f)
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.setDuration(600)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()

        appNameText?.animate()
            ?.alpha(1f)
            ?.translationY(0f)
            ?.setStartDelay(200)
            ?.setDuration(500)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()

        loadingText?.animate()
            ?.alpha(0.87f)
            ?.setStartDelay(400)
            ?.setDuration(400)
            ?.start()

        progressBar?.animate()
            ?.alpha(1f)
            ?.setStartDelay(600)
            ?.setDuration(400)
            ?.start()

        decorCircle1?.animate()
            ?.rotation(360f)
            ?.setDuration(20000)
            ?.setInterpolator(android.view.animation.LinearInterpolator())
            ?.withEndAction {
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

        versionText?.animate()
            ?.alpha(0.5f)
            ?.setStartDelay(800)
            ?.setDuration(400)
            ?.start()
    }

    private fun hasNetworkConnectivity(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
                ?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }

    private fun goToMain() {
        if (navigatedToMain) return
        // F3: don't navigate if Activity is already closing (back-press while ad is showing).
        if (isDestroyed || isFinishing) return
        val app = application as RoyApp
        if (app.isFullscreenAdShowing) {
            // Race guard: ProcessLifecycle's App Open is showing — defer navigation until dismissed.
            // F1: store listener reference so onDestroy can deregister, preventing a fire on dead Activity.
            // F6: skip if already deferred (no duplicate listeners).
            if (adDismissListener == null) {
                adDismissListener = { goToMain() }
                app.onFullscreenAdDismissed(adDismissListener!!)
                // F2: emergency escape — only post once. If ad is somehow stuck for >30s,
                // force-navigate regardless so user is never permanently stuck on splash.
                splashTimeoutHandler.postDelayed({
                    if (!navigatedToMain) goToMain()
                }, 30_000L)
            }
            return
        }
        navigatedToMain = true
        splashTimeoutHandler.removeCallbacks(splashTimeoutRunnable)
        val intent = Intent(this, MainAct::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        val r = Runnable { finish() }
        finishRunnable = r
        window.decorView.postDelayed(r, 300)
    }

    // Skip BaseAct.applyTheme() — SplashAct keeps its own SplashTheme from the manifest.
    // Applying AppTheme/AppThemeDark here would overwrite windowBackground and cause a
    // visible flash from the splash drawable to the app background color on cold start.
    override fun shouldApplyTheme(): Boolean = false

    // SplashAct extends BaseAct, so attachBaseContext chains directly to BaseAct (LocaleHelper)
    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
    }

    // onResume inherited from BaseAct (handles enableAdaptiveRefreshRate) — no override needed

    override fun onDestroy() {
        splashTimeoutHandler.removeCallbacksAndMessages(null)
        // F1: deregister dismiss listener to prevent firing on destroyed Activity
        adDismissListener?.let { (application as RoyApp).removeFullscreenAdDismissedListener(it) }
        adDismissListener = null
        finishRunnable?.let { window.decorView.removeCallbacks(it) }
        finishRunnable = null

        logoCard?.animate()?.cancel()
        appNameText?.animate()?.cancel()
        loadingText?.animate()?.cancel()
        progressBar?.animate()?.cancel()
        versionText?.animate()?.cancel()
        decorCircle1?.animate()?.cancel()
        decorCircle2?.animate()?.cancel()

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
