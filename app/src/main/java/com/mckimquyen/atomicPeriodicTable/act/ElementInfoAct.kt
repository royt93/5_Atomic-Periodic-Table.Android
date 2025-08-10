package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.MainAct
import com.mckimquyen.atomicPeriodicTable.act.setting.FavoritePageAct
import com.mckimquyen.atomicPeriodicTable.act.setting.SubmitAct
import com.mckimquyen.atomicPeriodicTable.ext.InfoExt
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.sdkadbmob.AdMobManager
import com.mckimquyen.atomicPeriodicTable.util.Utils
import kotlinx.android.synthetic.main.a_element_info.backBtn
import kotlinx.android.synthetic.main.a_element_info.commonTitleBack
import kotlinx.android.synthetic.main.a_element_info.detailEmission
import kotlinx.android.synthetic.main.a_element_info.detailEmissionBackground
import kotlinx.android.synthetic.main.a_element_info.frame
import kotlinx.android.synthetic.main.a_element_info.iBtn
import kotlinx.android.synthetic.main.a_element_info.nextBtn
import kotlinx.android.synthetic.main.a_element_info.offlineSpace
import kotlinx.android.synthetic.main.a_element_info.overviewInc
import kotlinx.android.synthetic.main.a_element_info.previousBtn
import kotlinx.android.synthetic.main.a_element_info.propertiesInc
import kotlinx.android.synthetic.main.a_element_info.scrView
import kotlinx.android.synthetic.main.a_element_info.shell
import kotlinx.android.synthetic.main.a_element_info.shellBackground
import kotlinx.android.synthetic.main.a_element_info.view
import kotlinx.android.synthetic.main.v_d_properties.electronView
import kotlinx.android.synthetic.main.v_d_properties.spImg
import kotlinx.android.synthetic.main.v_d_properties.spOffline
import kotlinx.android.synthetic.main.view_detail_emission.closeEmissionBtn
import kotlinx.android.synthetic.main.view_favorite_bar.editFavBtn
import kotlinx.android.synthetic.main.view_shell_view.closeShellBtn
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class ElementInfoAct : InfoExt() {

    //    private var adView: MaxAdView? = null
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        createAdInter()
        setupViews()
    }

    override fun onDestroy() {
//        flAdRoy.destroyAdBanner(adView)
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
//        val elementSendAndLoadPreference = ElementSendAndLoad(this)
//        var elementSendAndLoadValue = elementSendAndLoadPreference.getValue()
        setContentView(R.layout.a_element_info)
        Utils.fadeInAnim(scrView, 300)
        readJson()
        shell.visibility = View.GONE
        detailEmission.visibility = View.GONE
        detailViews()
        offlineCheck()
        nextPrev()
        favoriteBarSetup()
        elementAnim(overviewInc, propertiesInc)
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        backBtn.setOnClickListener {
            super.onBackPressed()
        }
        editFavBtn.setOnClickListener {
            val intent = Intent(this, FavoritePageAct::class.java)
            startActivity(intent)
        }
        iBtn.setOnClickListener {
            val intent = Intent(this, SubmitAct::class.java)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (shellBackground.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(shell, 300)
            Utils.fadeOutAnim(shellBackground, 300)
            return
        }
        if (detailEmission.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(detailEmission, 300)
            Utils.fadeOutAnim(detailEmissionBackground, 300)
            return
        } else {
//            showAd {
//                //do nothing
//            }
            super.onBackPressed()
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = frame.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        frame.layoutParams = params

        val paramsO = offlineSpace.layoutParams as ViewGroup.MarginLayoutParams
        paramsO.topMargin += top
        offlineSpace.layoutParams = paramsO

        val params2 = commonTitleBack.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        commonTitleBack.layoutParams = params2
    }

    @SuppressLint("SetTextI18n")
    private fun offlineCheck() {
        val offlinePreferences = OfflinePreference(this)
        val offlinePrefValue = offlinePreferences.getValue()

        if (offlinePrefValue == 1) {
            frame.visibility = View.GONE
            offlineSpace.visibility = View.VISIBLE
            spImg.visibility = View.GONE
            spOffline.visibility = View.VISIBLE
            spOffline.text = "Go online for emission lines"
        } else {
            frame.visibility = View.VISIBLE
            offlineSpace.visibility = View.GONE
            spImg.visibility = View.VISIBLE
            spOffline.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        favoriteBarSetup()
        adView?.resume()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    private fun detailViews() {
        electronView.setOnClickListener {
            Utils.fadeInAnim(shell, 300)
            Utils.fadeInAnim(shellBackground, 300)
        }
        closeShellBtn.setOnClickListener {
            Utils.fadeOutAnim(shell, 300)
            Utils.fadeOutAnim(shellBackground, 300)
        }
        shellBackground.setOnClickListener {
            Utils.fadeOutAnim(shell, 300)
            Utils.fadeOutAnim(shellBackground, 300)
        }
        spImg.setOnClickListener {
            Utils.fadeInAnim(detailEmission, 300)
            Utils.fadeInAnim(detailEmissionBackground, 300)
        }
        closeEmissionBtn.setOnClickListener {
            Utils.fadeOutAnim(detailEmission, 300)
            Utils.fadeOutAnim(detailEmissionBackground, 300)
        }
        detailEmissionBackground.setOnClickListener {
            Utils.fadeOutAnim(detailEmission, 300)
            Utils.fadeOutAnim(detailEmissionBackground, 300)
        }
    }

    private fun elementAnim(view: View, view2: View) {
        view.alpha = 0.0f
        view.animate().duration = 150
        view.animate().alpha(1.0f)
        val delay = Handler(Looper.getMainLooper())
        delay.postDelayed({
            view2.alpha = 0.0f
            view2.animate().duration = 150
            view2.animate().alpha(1.0f)
        }, 150)
    }

    private fun nextPrev() {
        nextBtn.setOnClickListener {
            val jsonString: String?
            try {
                val elementSendAndLoadPreference = ElementSendAndLoad(this)
                val elementSendAndLoadValue = elementSendAndLoadPreference.getValue()
                val ext = ".json"
                val elementJson = "$elementSendAndLoadValue$ext"
                val inputStream: InputStream = assets.open(elementJson)
                jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val currentNumb = jsonObject.optString("element_atomic_number", "---")
                val elements = ArrayList<Element>()
                ElementModel.getList(elements)
                val item = elements[currentNumb.toInt()]
                val elementSendAndLoad = ElementSendAndLoad(this)
                elementSendAndLoad.setValue(item.element)
                readJson()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        previousBtn.setOnClickListener {
            val jsonString: String?
            try {
                val elementSendAndLoadPreference = ElementSendAndLoad(this)
                val elementSendAndLoadValue = elementSendAndLoadPreference.getValue()
                val ext = ".json"
                val elementJson = "$elementSendAndLoadValue$ext"
                val inputStream: InputStream = assets.open(elementJson)
                jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                val currentNumb = jsonObject.optString("element_atomic_number", "---")
                val elements = ArrayList<Element>()
                ElementModel.getList(elements)
                val item = elements[currentNumb.toInt() - 2]
                val elementSendAndLoad = ElementSendAndLoad(this)
                elementSendAndLoad.setValue(item.element)
                readJson()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

//    private fun createAdInter() {
//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            interstitialAd = MaxInterstitialAd(getString(R.string.INTER), this)
//            interstitialAd?.let { ad ->
//                ad.setListener(object : MaxAdListener {
//                    override fun onAdLoaded(p0: MaxAd) {
////                        logI("onAdLoaded")
////                        retryAttempt = 0
//                    }
//
//                    override fun onAdDisplayed(p0: MaxAd) {
////                        logI("onAdDisplayed")
//                    }
//
//                    override fun onAdHidden(p0: MaxAd) {
////                        logI("onAdHidden")
//                        // Interstitial Ad is hidden. Pre-load the next ad
//                        interstitialAd?.loadAd()
//                    }
//
//                    override fun onAdClicked(p0: MaxAd) {
////                        logI("onAdClicked")
//                    }
//
//                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
////                        logI("onAdLoadFailed")
////                        retryAttempt++
////                        val delayMillis =
////                            TimeUnit.SECONDS.toMillis(2.0.pow(min(6, retryAttempt)).toLong())
////
////                        Handler(Looper.getMainLooper()).postDelayed(
////                            {
////                                interstitialAd?.loadAd()
////                            }, delayMillis
////                        )
//                    }
//
//                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
////                        logI("onAdDisplayFailed")
//                        // Interstitial ad failed to display. We recommend loading the next ad.
//                        interstitialAd?.loadAd()
//                    }
//
//                })
//                ad.setRevenueListener {
////                    logI("onAdDisplayed")
//                }
//
//                // Load the first ad.
//                ad.loadAd()
//            }
//        }
//    }
//
//    private fun showAd(runnable: Runnable? = null) {
//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            if (interstitialAd == null) {
//                runnable?.run()
//            } else {
//                interstitialAd?.let { ad ->
//                    if (ad.isReady) {
////                        showDialogProgress()
////                        setDelay(500.getRandomNumber() + 500) {
////                            hideDialogProgress()
////                            ad.showAd()
////                            runnable?.run()
////                        }
//                        if (BuildConfig.DEBUG) {
//                            Toast.makeText(
//                                this@ElementInfoAct,
//                                "interstitialAd showAd SUCCESSFULLY",
//                                Toast.LENGTH_SHORT
//                            )
//                                .show()
//                        } else {
//                            ad.showAd()
//                        }
//                        runnable?.run()
//                    } else {
//                        runnable?.run()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "Applovin show ad Inter in debug mode", Toast.LENGTH_SHORT).show()
//            runnable?.run()
//        }
//    }
}
