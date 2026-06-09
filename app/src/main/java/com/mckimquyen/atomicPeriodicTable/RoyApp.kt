package com.mckimquyen.atomicPeriodicTable

import android.app.Application
import android.widget.Toast
import com.roy.sdkadbmob.AdManager
import com.roy.sdkadbmob.AdSdkConfig
import com.google.android.gms.ads.MobileAds
import android.util.Log

class RoyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupAdmob()
        com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache.init(this)
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "$packageName onCreate", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdmob() {
        val adConfig = AdSdkConfig(
            isEnableAdmob          = BuildConfig.IS_ENABLE_ADMOB,
            isDebug                = BuildConfig.DEBUG,
            admobBannerId          = BuildConfig.ADMOB_BANNER_ID,
            admobInterstitialId    = BuildConfig.ADMOB_INTERSTITIAL_ID,
            admobAppOpenId         = BuildConfig.ADMOB_APP_OPEN_ID,
            applovinBannerId       = BuildConfig.APPLOVIN_BANNER_ID,
            applovinInterstitialId = BuildConfig.APPLOVIN_INTERSTITIAL_ID,
            applovinAppOpenId      = BuildConfig.APPLOVIN_APP_OPEN_ID
        )

        AdManager.setConfig(adConfig)
        AdManager.earlyInit(this)
        
        // Kích hoạt lắng nghe sự kiện đóng/mở background của toàn thiết bị để show App Open Ad
        AdManager.registerAppOpenAdLifecycle(this)

        if (BuildConfig.IS_ENABLE_ADMOB) {
            Log.d("RoyApp", "AdMob mode, initializing MobileAds")
            MobileAds.initialize(this) { 
                AdManager.init(this, adConfig) { success, gaid ->
                    Log.d("RoyApp", "AdManager init success=$success, gaid=$gaid")
                }
            }
        } else {
            Log.d("RoyApp", "AppLovin mode, initializing AppLovinSdk")
            // AppLovinSdk setup if using AppLovin
            val sdk = com.applovin.sdk.AppLovinSdk.getInstance(this)
            sdk.mediationProvider = "max"
            sdk.initializeSdk {
                AdManager.init(this, adConfig) { success, gaid ->
                    Log.d("RoyApp", "AdManager init success=$success, gaid=$gaid")
                }
            }
        }
    }
}
