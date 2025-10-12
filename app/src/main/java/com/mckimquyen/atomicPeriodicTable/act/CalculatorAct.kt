package com.mckimquyen.atomicPeriodicTable.act

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ACalculatorBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class CalculatorAct : AppCompatActivity() {

    // Khai báo binding cho ViewBinding
    private lateinit var binding: ACalculatorBinding

    override fun attachBaseContext(context: Context) {
        val override = Configuration(context.resources.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()
        if (themePrefValue == 0) {
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            setTheme(R.style.AppThemeDark)
        }

        // Khởi tạo ViewBinding
        binding = ACalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root) //Don't move down (Needs to be before we call our functions)

        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }

        initUi()
    }

    private fun initUi() {
        binding.editElement1.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                onClickSearch()
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun onClickSearch() {
        val mArray = resources.getStringArray(R.array.calcArray)
        val text = binding.editElement1.text.toString()

        for (i in 0 until 2) {
            var jsonString: String?
            if (text == mArray[i].toString()) {

                if (text == "H") {
                    val ext = ".json"
                    val elementJson = "1$ext"

                    val inputStream: InputStream = assets.open(elementJson)
                    jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)
                    val jsonObject: JSONObject = jsonArray.getJSONObject(0)
                    val elementAtomicWeight1 = jsonObject.optString("element_atomicmass", "---")

                    val final =
                        elementAtomicWeight1.toInt() * (binding.editNumber1.text.toString().toInt())

                    Toast.makeText(this, final, Toast.LENGTH_SHORT).show()
                }

            }
        }
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
