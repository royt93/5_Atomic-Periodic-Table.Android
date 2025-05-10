package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.sdkadbmob.AdMobManager

@SuppressLint("CustomSplashScreen")
class SplashAct : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AdMobManager.initSplashScreen(activity = this, onAdLoaded = {
            goToMain()
        })
    }

    private fun goToMain() {
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
}
