package com.mckimquyen.atomicPeriodicTable

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.AdSafetyLimits
import com.roy.sdkadbmob.AdSdkConfig

class RoyApp : Application() {
    private var adsInitializeStarted = false
    private var adsInitialized = false
    private val pendingAdInitCallbacks = mutableListOf<(Boolean, String?) -> Unit>()

    // Track AppLovin fullscreen activities (App Open, Interstitial) to detect the race
    // between ProcessLifecycle.showAppOpenAd and initSplashScreen on warm relaunch.
    var isFullscreenAdShowing = false
        private set
    private val fullscreenAdDismissListeners = mutableListOf<() -> Unit>()

    fun onFullscreenAdDismissed(listener: () -> Unit) {
        fullscreenAdDismissListeners.add(listener)
    }

    fun removeFullscreenAdDismissedListener(listener: () -> Unit) {
        fullscreenAdDismissListeners.remove(listener)
    }

    override fun onCreate() {
        super.onCreate()
        configureAds()
        registerFullscreenAdTracker()
        com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.init(this)
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "$packageName onCreate", Toast.LENGTH_SHORT).show()
        }
    }

    // Watches for AppLovinFullscreenActivity lifecycle to detect when a fullscreen ad
    // (App Open or Interstitial) is showing. Used by SplashAct.goToMain() to avoid
    // navigating to MainAct while ProcessLifecycle's App Open ad is still on screen.
    private fun registerFullscreenAdTracker() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(a: Activity, b: Bundle?) {
                if (a.javaClass.name.contains("AppLovinFullscreen")) {
                    isFullscreenAdShowing = true
                }
            }
            override fun onActivityDestroyed(a: Activity) {
                if (a.javaClass.name.contains("AppLovinFullscreen")) {
                    isFullscreenAdShowing = false
                    val listeners = fullscreenAdDismissListeners.toList()
                    fullscreenAdDismissListeners.clear()
                    listeners.forEach { it() }
                }
            }
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityResumed(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        })
    }

    private fun configureAds() {
        val adConfig = AdSdkConfig(
            isEnableAdmob          = BuildConfig.IS_ENABLE_ADMOB,
            isDebug                = BuildConfig.DEBUG,
            admobBannerId          = BuildConfig.ADMOB_BANNER_ID,
            admobInterstitialId    = BuildConfig.ADMOB_INTERSTITIAL_ID,
            admobAppOpenId         = BuildConfig.ADMOB_APP_OPEN_ID,
            admobRewardedId        = BuildConfig.ADMOB_REWARDED_ID,
            applovinBannerId       = BuildConfig.APPLOVIN_BANNER_ID,
            applovinInterstitialId = BuildConfig.APPLOVIN_INTERSTITIAL_ID,
            applovinAppOpenId      = BuildConfig.APPLOVIN_APP_OPEN_ID,
            applovinRewardedId     = BuildConfig.APPLOVIN_REWARDED_ID,
            safety                 = if (BuildConfig.DEBUG) AdSafetyLimits.TEST else AdSafetyLimits.CONTENT,
            vipKeySecret           = AdKeys.VIP_SECRET,
            applovinSdkKey         = BuildConfig.APPLOVIN_SDK_KEY,
        )

        AdManager.setConfig(adConfig)
    }

    fun initializeAdsIfNeeded(onComplete: (Boolean, String?) -> Unit) {
        if (adsInitialized) {
            onComplete(true, null)
            return
        }
        if (adsInitializeStarted) {
            pendingAdInitCallbacks += onComplete
            return
        }
        adsInitializeStarted = true
        pendingAdInitCallbacks += onComplete
        AdManager.initialize(this) { success, gaid ->
            adsInitialized = success
            adsInitializeStarted = false
            Log.d("RoyApp", "AdManager initialize success=$success, gaid=$gaid")
            val callbacks = pendingAdInitCallbacks.toList()
            pendingAdInitCallbacks.clear()
            callbacks.forEach { it(success, gaid) }
        }
    }
}
