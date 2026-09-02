package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
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
import com.mckimquyen.atomicPeriodicTable.ext.shareImage
import com.mckimquyen.atomicPeriodicTable.feature.share.ElementCardRenderer
import com.mckimquyen.atomicPeriodicTable.feature.tts.TtsAvailability
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.ElementTranslator
import com.mckimquyen.atomicPeriodicTable.util.ElementWeightCache
import com.mckimquyen.atomicPeriodicTable.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import androidx.core.view.isVisible

class ElementInfoAct : InfoExt() {

    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        createAdInter()
        setupViews()
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        super.onDestroy()
    }


    private fun setupViews() {
        binding = AElementInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.fadeInAnim(binding.scrView, 300)

        binding.notesInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val isNotEmpty = s?.toString()?.trim()?.isNotEmpty() == true
                binding.notesSaveBtn.isEnabled = isNotEmpty
                binding.notesSaveBtn.alpha = if (isNotEmpty) 1.0f else 0.5f
            }
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
            notesPref.saveNote(elementSendAndLoadValue.lowercase(java.util.Locale.US), textToSave)
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
        binding.shareElementBtn.setOnClickListener {
            shareCurrentElement()
        }

        binding.speakElementBtn.isEnabled = false
        binding.speakElementBtn.alpha = 0.4f
        textToSpeech = TextToSpeech(this) { status ->
            val languageResult = if (status == TextToSpeech.SUCCESS) textToSpeech?.setLanguage(Locale.US) else null
            ttsReady = TtsAvailability.isUsable(status, languageResult)
            binding.speakElementBtn.isEnabled = ttsReady
            binding.speakElementBtn.alpha = if (ttsReady) 1f else 0.4f
        }
        binding.speakElementBtn.setOnClickListener {
            speakCurrentElementName()
        }

        refreshVipGatedBanner(
            container = findViewById(R.id.bannerContainer),
            tvLabelAd = findViewById(R.id.tvLabelAd),
        )
    }


    /** Speaks the raw English element name (not the localized display name) — pronunciation
     * stays reliable regardless of whether the device's TTS engine has a voice installed for
     * the app's current UI locale. */
    private fun speakCurrentElementName() {
        if (!ttsReady) return
        val elementName = ElementSendAndLoad(this).getValue() ?: return
        textToSpeech?.speak(elementName, TextToSpeech.QUEUE_FLUSH, null, "element_name_utterance")
    }

    private fun shareCurrentElement() {
        val elementName = ElementSendAndLoad(this).getValue() ?: return
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val element = elements.find { it.element == elementName } ?: return

        ElementWeightCache.init(this)
        val massText = ElementWeightCache.getMass(element.short)?.let { "%.2f u".format(it) } ?: "---"
        val categoryRaw = ElementWeightCache.getCategory(element.short) ?: "---"
        val categoryText = com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator.translate(this, categoryRaw)

        val bitmap = ElementCardRenderer.render(
            context = this,
            symbol = element.short,
            name = ElementTranslator.getLocalizedName(this, element.element),
            number = element.number,
            massText = massText,
            categoryText = categoryText,
        )
        shareImage(bitmap, "element_${element.short}.png")
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
        // FIX-013: re-sync banner with current VIP state (see BaseAct.refreshVipGatedBanner).
        refreshVipGatedBanner(
            container = findViewById(R.id.bannerContainer),
            tvLabelAd = findViewById(R.id.tvLabelAd),
        )
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
                Log.e("ElementInfoAct", "nextBtn: failed to load next element JSON", e)
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
                Log.e("ElementInfoAct", "previousBtn: failed to load previous element JSON", e)
            }
        }
    }
}
