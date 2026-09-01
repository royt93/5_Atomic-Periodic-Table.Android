package com.mckimquyen.atomicPeriodicTable.ext

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.pref.TemperatureUnits
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.databinding.AMainBinding
import com.mckimquyen.atomicPeriodicTable.util.ToastUtil
import com.mckimquyen.atomicPeriodicTable.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale

abstract class TableExt : BaseAct(), View.OnApplyWindowInsetsListener {
    protected lateinit var binding: AMainBinding

    private var systemUiConfigured = false

    // Handler instances stored as protected members for memory leak prevention
    // Subclasses can access these if needed, and they will be cleaned up in onDestroy()
    // initBoiling/initMelting/initPhase/initYear/initWeight/initHeat/initSpecific/initVape
    // only ever run one-at-a-time (one hover-menu button click at a time), so they share
    // a single handler via initJsonField() below. initElectro/initGroups have real
    // per-color branching logic and keep their own handlers.
    protected var jsonFieldHandler: Handler? = null
    protected var electroHandler: Handler? = null
    protected var groupsHandler: Handler? = null

    override fun onStart() {
        super.onStart()
        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener(this)

        if (!systemUiConfigured) {
            systemUiConfigured = true
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) = Unit

    override fun onApplyWindowInsets(v: View, insets: android.view.WindowInsets): android.view.WindowInsets {
        // Modern API: Use WindowInsetsCompat instead of deprecated WindowInsets methods
        val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets, v)
        val systemBars = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())

        onApplySystemInsets(
            systemBars.top,
            systemBars.bottom,
            systemBars.left,
            systemBars.right
        )
        return insets
    }

    private fun closeHover() {
        Utils.fadeOutAnim(binding.hoverBackground, 200)
        Utils.fadeOutAnim(binding.hoverMenuInclude.root, 300)
    }

    @SuppressLint("DiscouragedApi")
    fun initName(list: ArrayList<Element>) {
        for (item in list) {
            val name = item.element
            closeHover()
            val extText = "_text"
            val eView = "$name$extText"
            val extBtn = "_btn"
            val eViewBtn = "$name$extBtn"
            val resID = resources.getIdentifier(eView, "id", packageName)
            val resIDB = resources.getIdentifier(eViewBtn, "id", packageName)

            val text = findViewById<TextView>(resID)
            text.text = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(this, item.element)
            val btn = findViewById<TextView>(resIDB)
            val themePref = ThemePref(this)
            val themePrefValue = themePref.getValue()

            val params = text.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin = 0
            params.rightMargin = 0
            params.bottomMargin = resources.getDimensionPixelSize(R.dimen.groups2b)
            text.layoutParams = params
            text.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            btn.elevation = (resources.getDimension(R.dimen.zero_elevation))
            binding.vGroup3.lanthanoidsBtn.elevation = (resources.getDimension(R.dimen.zero_elevation))
            binding.vGroup3.actinoidsBtn.elevation = (resources.getDimension(R.dimen.zero_elevation))

            if (themePrefValue == 100) {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        btn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
                        binding.vGroup3.lanthanoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
                        binding.vGroup3.actinoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
                    }

                    Configuration.UI_MODE_NIGHT_YES -> {
                        btn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
                        binding.vGroup3.lanthanoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
                        binding.vGroup3.actinoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
                    }
                }
            }
            if (themePrefValue == 0) {
                btn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
                binding.vGroup3.lanthanoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
                binding.vGroup3.actinoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_light))
            }
            if (themePrefValue == 1) {
                btn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
                binding.vGroup3.lanthanoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
                binding.vGroup3.actinoidsBtn.background.mutate().setTint(ContextCompat.getColor(this, R.color.element_box_dark))
            }
        }
    }

    /**
     * Shared body for the 8 hover-menu fields that just copy one JSON key into each
     * element's TextView (boiling/melting/phase/year/weight/heat/specific/vape) — only
     * the key differs. initElectro/initGroups keep their own bodies (real per-color logic).
     */
    @SuppressLint("DiscouragedApi")
    private fun initJsonField(list: ArrayList<Element>, jsonKey: (JSONObject) -> String) {
        initName(list)
        closeHover()
        jsonFieldHandler = Handler(Looper.getMainLooper())
        jsonFieldHandler?.postDelayed({
            for (item in list) {
                val name = item.element
                val iText =
                    findViewById<TextView>(resources.getIdentifier("${name}_text", "id", packageName))
                try {
                    val inputStream: InputStream = assets.open("$name.json")
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jsonObject: JSONObject = JSONArray(jsonString).getJSONObject(0)
                    iText.text = jsonKey(jsonObject)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 10)
    }

    fun initBoiling(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_boiling_${TemperatureUnits(this).getValue()}", "---")
    }

    fun initMelting(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_melting_${TemperatureUnits(this).getValue()}", "---")
    }

    fun initPhase(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_phase", "---")
    }

    fun initYear(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_year", "---")
    }

    @SuppressLint("DiscouragedApi")
    fun initElectro(list: ArrayList<Element>) {
        electroHandler = Handler(Looper.getMainLooper())
        initName(list)
        closeHover()
        electroHandler?.postDelayed({
            for (item in list) {
                val name = item.element
                val extText = "_text"
                val eView = "$name$extText"
                val extBtn = "_btn"
                val eViewBtn = "$name$extBtn"
                val resID = resources.getIdentifier(eView, "id", packageName)
                val resIDB = resources.getIdentifier(eViewBtn, "id", packageName)
                if (resID == 0) {
                    ToastUtil.showToast(this, "Error on find IdView")
                } else {
                    if (item.electro == 0.0) {
                        val text = findViewById<TextView>(resID)
                        text.text = "---"
                    } else {
                        val text = findViewById<TextView>(resID)
                        text.text = (item.electro).toString()
                    }
                }
                if (resIDB == 0) {
                    ToastUtil.showToast(this, "Error on find IdView")
                } else {
                    if (item.electro == 0.0) {
                        val btn = findViewById<TextView>(resIDB)
                        val themePref = ThemePref(this)
                        val themePrefValue = themePref.getValue()

                        if (themePrefValue == 100) {
                            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    btn.background.mutate().setTint(Color.argb(255, 254, 254, 254))
                                }

                                Configuration.UI_MODE_NIGHT_YES -> {
                                    btn.background.mutate().setTint(Color.argb(255, 18, 18, 18))
                                }
                            }
                        }
                        if (themePrefValue == 0) {
                            btn.background.mutate().setTint(Color.argb(255, 254, 254, 254))
                        }
                        if (themePrefValue == 1) {
                            btn.background.mutate().setTint(Color.argb(255, 18, 18, 18))
                        }
                    } else {
                        if (item.electro > 1) {
                            val btn = findViewById<TextView>(resIDB)
                            btn.background.mutate().setTint(
                                Color.argb(
                                    255,
                                    255,
                                    225.div(item.electro).toInt(),
                                    0
                                )
                            )
                        } else {
                            val btn = findViewById<TextView>(resIDB)
                            btn.background.mutate().setTint(Color.argb(255, 255, 214, 0))
                        }
                    }
                }
            }
        }, 10)
    }

    @SuppressLint("DiscouragedApi")
    fun initGroups(list: ArrayList<Element>) {
        groupsHandler = Handler(Looper.getMainLooper())
        initName(list)
        groupsHandler?.postDelayed({
            for (item in list) {
                closeHover()
                val name = item.element
                val extBtn = "_btn"
                val extText = "_text"
                val eViewBtn = "$name$extBtn"
                val eText = "$name$extText"
                val resIDB = resources.getIdentifier(eViewBtn, "id", packageName)
                val resID = resources.getIdentifier(eText, "id", packageName)

                val iText = findViewById<TextView>(resID)
                var jsonstring: String?
                try {
                    val ext = ".json"
                    val elementJson = "$name$ext"
                    val inputStream: InputStream = assets.open(elementJson)
                    jsonstring = inputStream.bufferedReader().use { it.readText() }

                    val jsonArray = JSONArray(jsonstring)
                    val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                    val elementGroup = jsonObject.optString("element_group", "---")
                    iText.text = elementGroup
                    val params = iText.layoutParams as ViewGroup.MarginLayoutParams
                    params.leftMargin = resources.getDimensionPixelSize(R.dimen.groups)
                    params.rightMargin = resources.getDimensionPixelSize(R.dimen.groups)
                    params.bottomMargin = resources.getDimensionPixelSize(R.dimen.groupsb)
                    iText.layoutParams = params
                    iText.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    iText.requestLayout()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val btn = findViewById<TextView>(resIDB)
                if ((item.number == 3) or (item.number == 11) or (item.number == 19) or (item.number == 37) or (item.number == 55) or (item.number == 87)) {
                    btn.background.mutate().setTint(Color.argb(255, 255, 102, 102))
                }
                if ((item.number == 4) or (item.number == 12) or (item.number == 20) or (item.number == 38) or (item.number == 56) or (item.number == 88)) {
                    btn.background.mutate().setTint(Color.argb(255, 255, 195, 112))
                }
                if ((item.number in 21..30) or (item.number in 39..48) or (item.number in 72..80) or (item.number in 104..112)) {
                    btn.background.mutate().setTint(Color.argb(255, 225, 168, 166))
                }
                if ((item.number == 5) or (item.number == 14) or (item.number in 32..33) or (item.number in 51..52) or (item.number == 85)) {
                    btn.background.mutate().setTint(Color.argb(255, 184, 184, 136))
                }
                if ((item.number == 13) or (item.number == 31) or (item.number in 49..50) or (item.number in 81..84) or (item.number in 113..118)) {
                    btn.background.mutate().setTint(Color.argb(255, 174, 174, 174))
                }
                if ((item.number == 53) or (item.number in 34..35) or (item.number in 15..17) or (item.number in 6..9) or (item.number == 1)) {
                    btn.background.mutate().setTint(Color.argb(255, 129, 199, 132))
                }
                if ((item.number == 2) or (item.number == 10) or (item.number == 18) or (item.number == 36) or (item.number == 54) or (item.number == 86)) {
                    btn.background.mutate().setTint(Color.argb(255, 97, 193, 193))
                }
            }
        }, 10)
    }

    fun initWeight(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_atomicmass", "---")
    }

    fun initHeat(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_fusion_heat", "---")
    }

    fun initSpecific(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_specific_heat_capacity", "---")
    }

    fun initVape(list: ArrayList<Element>) = initJsonField(list) {
        it.optString("element_vaporization_heat", "---")
    }

    /**
     * Protected cleanup method for Handler instances.
     * Subclasses should call super.onDestroy() to ensure proper cleanup.
     */
    override fun onDestroy() {
        // Clean up all handlers to prevent memory leaks
        jsonFieldHandler?.removeCallbacksAndMessages(null)
        jsonFieldHandler = null
        electroHandler?.removeCallbacksAndMessages(null)
        electroHandler = null
        groupsHandler?.removeCallbacksAndMessages(null)
        groupsHandler = null
        super.onDestroy()
    }

}
