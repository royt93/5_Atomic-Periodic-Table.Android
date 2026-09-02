package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.text.isDigitsOnly
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.databinding.ANuclideBinding
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class NuclideAct : BaseAct() {
    companion object {
        // FIX-033: MIN_SCALE bounds how far pinch-out can zoom in (1f/0.4f = 2.5x); MAX_SCALE
        // keeps the existing "can't shrink below original size" behavior (1f/1f = 1x).
        private const val MIN_SCALE = 0.4f
        private const val MAX_SCALE = 1f
    }

    private lateinit var binding: ANuclideBinding
    private val elementLists = ArrayList<Element>()
    var mScale = 1f
    private lateinit var mScaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    // Handler instance for memory leak prevention
    private var addViewsHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
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
        binding = ANuclideBinding.inflate(layoutInflater)
        setContentView(binding.root) //REMEMBER: Never move any function calls above this

        binding.viewStub.inflate()

        runOnUiThread {
            binding.ldnPlace.root.visibility = View.VISIBLE
        }

        addViewsHandler = Handler(Looper.getMainLooper())
        addViewsHandler?.postDelayed({
            addViews(elementLists)
        }, 100)

        gestureDetector = GestureDetector(this, GestureListener())
        mScaleDetector = ScaleGestureDetector(
            /* context = */ this,
            /* listener = */ object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scale = 1 - detector.scaleFactor
                    val pScale = mScale
                    // FIX-033: mScale += scale was applied twice per callback, then both
                    // clamp branches forced mScale back to exactly 1f on ANY deviation —
                    // together that made pinch-zoom a no-op (mScale always snapped to 1f).
                    mScale += scale
                    if (mScale < MIN_SCALE)
                        mScale = MIN_SCALE
                    if (mScale > MAX_SCALE)
                        mScale = MAX_SCALE
                    val scaleAnimation = ScaleAnimation(
                        /* fromX = */ 1f / pScale,
                        /* toX = */ 1f / mScale,
                        /* fromY = */ 1f / pScale,
                        /* toY = */ 1f / mScale,
                        /* pivotX = */ detector.focusX,
                        /* pivotY = */ detector.focusY
                    )
                    scaleAnimation.duration = 0
                    scaleAnimation.fillAfter = true
                    scaleAnimation.willChangeBounds()
                    scaleAnimation.willChangeTransformationMatrix()
                    val layout = binding.scrollNuc as LinearLayoutCompat?
                    layout?.startAnimation(scaleAnimation)

                    return true
                }
            })

        binding.seekBarNuc.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
                val scaleAnimation = ScaleAnimation(
                    /* fromX = */ 1f / currentValue,
                    /* toX = */ 1f / currentValue,
                    /* fromY = */ 1f / currentValue,
                    /* toY = */ 1f / currentValue
                )
                scaleAnimation.duration = 0
                scaleAnimation.fillAfter = true
                // binding.scrollNuc is LinearLayoutCompat (no cast needed, ViewBinding provides correct type)
                binding.scrollNuc.startAnimation(scaleAnimation)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

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

        // Đăng ký callback để xử lý back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kết thúc activity và quay về màn hình trước
                finish()
            }
        })

        // Click listener cho nút back trên UI
        binding.nucBackBtn.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        // FIX-032: mScaleDetector.onTouchEvent(event) was called twice for the same
        // MotionEvent (once discarded, once returned) — ScaleGestureDetector fires its
        // onScale() callback for each call, so every real touch event doubled mScale's delta.
        val scaleHandled = mScaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return scaleHandled
    }

    private class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addViews(list: ArrayList<Element>) {
        ElementModel.getList(list)
        // Cache layout references để tránh multiple findViewById calls
        val nuclideContainer = findViewById<RelativeLayout>(R.id.nuc_view)
        val inflate = layoutInflater
        val mLayout: View = inflate.inflate(R.layout.view_item_nuclide, nuclideContainer, false)
        val param = RelativeLayout.LayoutParams(
            /* w = */ resources.getDimensionPixelSize(R.dimen.item_nuclide),
            /* h = */ resources.getDimensionPixelSize(R.dimen.item_nuclide)
        )
        // Position cho neutron (n) ở tọa độ (0, 1) trong nuclide chart
        param.leftMargin = 0 // Column 0 (Z=0 for neutron)
        param.topMargin = resources.getDimensionPixelSize(R.dimen.item_nuclide) // Row 1 (N=1)
        val s: TextView = mLayout.findViewById(R.id.tvnNuclideElement)
        val t: TextView = mLayout.findViewById(R.id.tvNuclideNumber)
        s.text = "n"
        t.text = "1"
        nuclideContainer.addView(mLayout, param)
        binding.ldnPlace.root.visibility = View.GONE

        for (item in list) {
            var jsonString: String?
            try {
                val hyd = item.element
                val ext = ".json"
                val elementJson = "$hyd$ext"
                val inputStream: InputStream = assets.open(elementJson)
                jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val jsonObject: JSONObject = jsonArray.getJSONObject(0)

                for (i in 1..item.isotopes) {
                    val isoN = "iso_N_"
                    val isoZ = "iso_Z_"
                    // val isoHalf = "iso_half_" // Unused - half-life data not displayed
                    val decayType = "decay_type_"
                    val number = i.toString()
                    val nJson = "$isoN$number"
                    val zJson = "$isoZ$number"
                    // val halfJson = "$isoHalf$number" // Unused
                    val decayTypeString = "$decayType$number"
                    val n = jsonObject.optString(nJson, "-")
                    val z = jsonObject.optString(zJson, "-")
                    // val half = jsonObject.optString(halfJson, "-") // Unused - half-life not shown

                    val decayTypeResult = jsonObject.optString(decayTypeString, "default")
                    // Reuse cached nuclideContainer thay vì findViewById trong loop (performance)
                    val myLayout: View =
                        inflate.inflate(R.layout.view_item_nuclide, nuclideContainer, false)
                    val params = RelativeLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.item_nuclide),
                        resources.getDimensionPixelSize(R.dimen.item_nuclide)
                    )

                    if (n.isDigitsOnly() && z.isDigitsOnly()) {
                        params.leftMargin =
                            resources.getDimensionPixelSize(R.dimen.item_nuclide) * (z.toInt())
                        params.topMargin =
                            resources.getDimensionPixelSize(R.dimen.item_nuclide) * (n.toInt())
                        val short: TextView = myLayout.findViewById(R.id.tvnNuclideElement)
                        val top: TextView = myLayout.findViewById(R.id.tvNuclideNumber)
                        val frame: FrameLayout = myLayout.findViewById(R.id.itemNuclideFrame)
                        val decay: TextView = myLayout.findViewById(R.id.tvNuclideDecay)

                        short.text = item.short
                        top.text = (z.toInt() + n.toInt()).toString()
                        decay.text = decayTypeResult
                        if (decayTypeResult == "stable") {
                            frame.background.mutate().setTint(Color.argb(255, 42, 50, 61))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                        }
                        if (decayTypeResult == "3p") {
                            frame.background.mutate().setTint(Color.argb(255, 137, 0, 7))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                        }
                        if (decayTypeResult == "2p") {
                            frame.background.mutate().setTint(Color.argb(255, 154, 0, 7))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                        }
                        if (decayTypeResult == "p") {
                            frame.background.mutate().setTint(Color.argb(255, 211, 47, 47))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                        }
                        if (decayTypeResult == "B+") {
                            frame.background.mutate().setTint(Color.argb(255, 211, 102, 89))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "2B-") {
                            frame.background.mutate().setTint(Color.argb(255, 3, 155, 229))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "B-") {
                            frame.background.mutate().setTint(Color.argb(255, 89, 204, 255))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "n") {
                            frame.background.mutate().setTint(Color.argb(255, 78, 186, 170))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "2n") {
                            frame.background.mutate().setTint(Color.argb(255, 0, 137, 123))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "a") {
                            frame.background.mutate().setTint(Color.argb(255, 255, 235, 59))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        if (decayTypeResult == "e- capture") {
                            frame.background.mutate().setTint(Color.argb(255, 176, 0, 78))
                            short.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                            top.setTextColor(ContextCompat.getColor(this, R.color.colorDarkPrimary))
                        }
                        nuclideContainer.addView(myLayout, params)
                    }
                }
            } catch (e: Exception) {
                Log.e("NuclideAct", "Failed to render nuclide grid", e)
            }
        }

    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        binding.scrollViewNuc.setPadding(
            /* left = */ 0,
            /* top = */ resources.getDimensionPixelSize(R.dimen.title_bar) + top,
            /* right = */ 0,
            /* bottom = */ resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        val params2 = binding.commonTitleBackNuc.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackNuc.layoutParams = params2
    }

    override fun onDestroy() {
        // Clean up handler to prevent memory leaks
        addViewsHandler?.removeCallbacksAndMessages(null)
        addViewsHandler = null
        super.onDestroy()
    }

}
