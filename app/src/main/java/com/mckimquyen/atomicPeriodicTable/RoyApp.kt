package com.mckimquyen.atomicPeriodicTable

import android.app.Application
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import com.mckimquyen.atomicPeriodicTable.sdkadbmob.AdMobManager
import com.mckimquyen.atomicPeriodicTable.sdkadbmob.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//TODO roy93~ firebase

//done
//review in app bingo
//120hz
//rate app
//more app
//share app
//policy
//manifest ad id
//leak canary
//ic launcher
//splash screen xml
//keystore
//github 20 tester
//ad applovin
//font scale

//done
class RoyApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        this.setupApplovinAd()
        setupAdmob()
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "$packageName onCreate", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdmob() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@RoyApp) {}
            AdMobManager.init(this@RoyApp) { success, gaidCurrent ->
                Logger.i("AdMobManager init success $success, gaidCurrent $gaidCurrent")
            }
        }
//        registerActivityLifecycleCallbacks(
//            AppLifecycleListener(
//                { isForeground, activity ->
//                    if (isForeground) {
//                        Logger.i("App moved to Foreground")
//                        Logger.i("activity.localClassName ${activity.localClassName}")
//                        Logger.i(
//                            "roy93~",
//                            "SplashActivity::class.java.simpleName ${SplashAct::class.java.simpleName}"
//                        )
//                        if (activity.localClassName == SplashAct::class.java.simpleName) {
//                            //do nothing
//                        } else {
////                            AdMobManager.showAppOpenAd(activity)
//                        }
//                    } else {
//                        Logger.i("App moved to Background")
//                    }
//                }, { activity ->
//                    Logger.i("callbackActivityCreated ${activity.localClassName}")
//                    if (activity.localClassName == SplashAct::class.java.simpleName) {
//                        //do nothing
//                    } else {
////                        AdMobManager.loadAppOpenAd(
////                            context = this,
////                            adUnitId = BuildConfig.ADMOB_APP_OPEN_ID,
////                            onAdLoaded = {},
////                        )
//                    }
//                }
//            )
//        )
    }
}
