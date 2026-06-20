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
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.setting.FavoritePageAct
import com.mckimquyen.atomicPeriodicTable.act.setting.SubmitAct
import com.mckimquyen.atomicPeriodicTable.databinding.AElementInfoBinding
import com.mckimquyen.atomicPeriodicTable.ext.InfoExt
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.roy.sdkadbmob.AdManager
import com.mckimquyen.atomicPeriodicTable.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import androidx.core.view.isVisible

class ElementInfoAct : InfoExt() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        createAdInter()
        setupViews()
    }


    private fun setupViews() {
        binding = AElementInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.fadeInAnim(binding.scrView, 300)

        binding.notesInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isNotEmpty = s?.toString()?.trim()?.isNotEmpty() == true
                if (binding.notesSaveBtn.isEnabled != isNotEmpty) {
                    binding.notesSaveBtn.isEnabled = isNotEmpty
                    if (isNotEmpty) {
                        // Playful spring scale-up bounce animation
                        binding.notesSaveBtn.animate()
                            .alpha(1.0f)
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(180)
                            .withEndAction {
                                binding.notesSaveBtn.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    } else {
                        // Smooth scale-down & fade
                        binding.notesSaveBtn.animate()
                            .alpha(0.5f)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(180)
                            .start()
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        readJson()
        binding.shell.root.visibility = View.GONE
        binding.detailEmission.root.visibility = View.GONE
        detailViews()
        offlineCheck()
        nextPrev()
        favoriteBarSetup()
        elementAnim(binding.overviewInc.root, binding.propertiesInc.root)

        binding.notesSaveBtn.setOnClickListener {
            val elementSendAndLoadPreference = ElementSendAndLoad(this)
            val elementSendAndLoadValue = elementSendAndLoadPreference.getValue() ?: ""
            val notesPref = com.mckimquyen.atomicPeriodicTable.pref.NotesPref(this)
            val textToSave = binding.notesInput.text.toString().trim()
            notesPref.saveNote(elementSendAndLoadValue, textToSave)
            android.widget.Toast.makeText(this, getString(R.string.notes_saved), android.widget.Toast.LENGTH_SHORT).show()

            // Clear focus and hide soft keyboard
            binding.notesInput.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.notesInput.windowToken, 0)
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

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press với logic đóng panel trước
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Nếu shell panel đang hiển thị, đóng nó lại
                if (binding.shellBackground.isVisible) {
                    Utils.fadeOutAnim(binding.shell.root, 300)
                    Utils.fadeOutAnim(binding.shellBackground, 300)
                    return
                }
                // Nếu emission panel đang hiển thị, đóng nó lại
                if (binding.detailEmission.root.isVisible) {
                    Utils.fadeOutAnim(binding.detailEmission.root, 300)
                    Utils.fadeOutAnim(binding.detailEmissionBackground, 300)
                    return
                }
                // Không có panel nào mở, kết thúc activity
                finish()
            }
        })

        // Click listener cho nút back trên UI
        binding.backBtn.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
        binding.favoriteBarInclude.editFavBtn.setOnClickListener {
            val intent = Intent(this, FavoritePageAct::class.java)
            startActivity(intent)
        }
        binding.iBtn.setOnClickListener {
            val intent = Intent(this, SubmitAct::class.java)
            startActivity(intent)
        }

        val bannerContainer = findViewById<ViewGroup>(R.id.bannerContainer)
        val tvLabelAd = findViewById<TextView>(R.id.tvLabelAd)
        if (!AdManager.isVipByKeyActive()) {
            AdManager.loadBanner(
                context = this,
                container = bannerContainer,
                tvLabelAd = tvLabelAd,
                adSize = AdManager.getAdaptiveBannerSize(this),
                autoManageLifecycle = true,
            )
        }
    }


    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = binding.frame.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 0
        binding.frame.layoutParams = params

        val params2 = binding.commonTitleBack.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBack.layoutParams = params2

        binding.scrView.setPadding(0, 0, 0, bottom)
    }

    @SuppressLint("SetTextI18n")
    private fun offlineCheck() {
        val offlinePreferences = OfflinePreference(this)
        val offlinePrefValue = offlinePreferences.getValue()

        if (offlinePrefValue == 1) {
            binding.frame.visibility = View.GONE
            binding.propertiesInc.spImg.visibility = View.GONE
            binding.propertiesInc.spOffline.visibility = View.VISIBLE
            binding.propertiesInc.spOffline.text = "Go online for emission lines"
        } else {
            binding.frame.visibility = View.VISIBLE
            binding.propertiesInc.spImg.visibility = View.VISIBLE
            binding.propertiesInc.spOffline.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        favoriteBarSetup()
    }

    private fun detailViews() {
        binding.propertiesInc.electronView.setOnClickListener {
            Utils.fadeInAnim(binding.shell.root, 300)
            Utils.fadeInAnim(binding.shellBackground, 300)
        }
        binding.shell.closeShellBtn.setOnClickListener {
            Utils.fadeOutAnim(binding.shell.root, 300)
            Utils.fadeOutAnim(binding.shellBackground, 300)
        }
        binding.shellBackground.setOnClickListener {
            Utils.fadeOutAnim(binding.shell.root, 300)
            Utils.fadeOutAnim(binding.shellBackground, 300)
        }
        binding.propertiesInc.spImg.setOnClickListener {
            Utils.fadeInAnim(binding.detailEmission.root, 300)
            Utils.fadeInAnim(binding.detailEmissionBackground, 300)
        }
        binding.detailEmission.closeEmissionBtn.setOnClickListener {
            Utils.fadeOutAnim(binding.detailEmission.root, 300)
            Utils.fadeOutAnim(binding.detailEmissionBackground, 300)
        }
        binding.detailEmissionBackground.setOnClickListener {
            Utils.fadeOutAnim(binding.detailEmission.root, 300)
            Utils.fadeOutAnim(binding.detailEmissionBackground, 300)
        }
    }

    private fun elementAnim(view: View, view2: View) {
        view.alpha = 0.0f
        view.animate().duration = 150
        view.animate().alpha(1.0f)
        // Memory leak fix: Use View.postDelayed instead of Handler
        // This ties the callback to the View's lifecycle
        view.postDelayed({
            view2.alpha = 0.0f
            view2.animate().duration = 150
            view2.animate().alpha(1.0f)
        }, 150)
    }

    private fun nextPrev() {
        binding.nextBtn.setOnClickListener {
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
            } catch (e: Exception) {
                // Catch Exception (not just IOException): JSONArray/getJSONObject
                // also throw JSONException which is not a subclass of IOException
                e.printStackTrace()
            }
        }
        binding.previousBtn.setOnClickListener {
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
            } catch (e: Exception) {
                // Catch Exception (not just IOException): JSONArray/getJSONObject
                // also throw JSONException which is not a subclass of IOException
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
