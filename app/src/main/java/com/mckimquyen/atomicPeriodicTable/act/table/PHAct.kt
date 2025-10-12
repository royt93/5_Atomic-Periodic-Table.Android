package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.APhBinding
import com.mckimquyen.atomicPeriodicTable.model.Indicator
import com.mckimquyen.atomicPeriodicTable.model.IndicatorModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class PHAct : BaseAct() {
    private lateinit var binding: APhBinding

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
        binding = APhBinding.inflate(layoutInflater)
        setContentView(binding.root)

        indicatorListener()

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
        binding.backBtnPh.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun indicatorListener() {
        val indicatorList = ArrayList<Indicator>()
        IndicatorModel.getList(indicatorList)
        val acidText = "[H+]>[OH-] pH<"
        val neutralText = "[H+]=[OH-] pH="
        val alkalineText = "[H+]<[OH-] pH>"

        val item = indicatorList[0]
        binding.acidInfo.text = acidText + item.acid
        binding.neutralInfo.text = neutralText + item.neutral
        binding.alkalineInfo.text = alkalineText + item.alkali
        updatePhColor(item)
        updateButtonColor("bromothymol_blue_btn")

        binding.phChipBar.bromothymolBlueBtn.setOnClickListener {
            val item = indicatorList[0]
            binding.acidInfo.text = acidText + item.acid
            binding.neutralInfo.text = neutralText + item.neutral
            binding.alkalineInfo.text = alkalineText + item.alkali
            updatePhColor(item)
            updateButtonColor("bromothymol_blue_btn")
        }
        binding.phChipBar.methylOrangeBtn.setOnClickListener {
            val item = indicatorList[1]
            binding.acidInfo.text = acidText + item.acid
            binding.neutralInfo.text = neutralText + item.neutral
            binding.alkalineInfo.text = alkalineText + item.alkali
            updatePhColor(item)
            updateButtonColor("methyl_orange_btn")
        }
        binding.phChipBar.congoRedBtn.setOnClickListener {
            val item = indicatorList[2]
            binding.acidInfo.text = acidText + item.acid
            binding.neutralInfo.text = neutralText + item.neutral
            binding.alkalineInfo.text = alkalineText + item.alkali
            updatePhColor(item)
            updateButtonColor("congo_red_btn")
        }
        binding.phChipBar.phenolphthaleinBtn.setOnClickListener {
            val item = indicatorList[3]
            binding.acidInfo.text = acidText + item.acid
            binding.neutralInfo.text = neutralText + item.neutral
            binding.alkalineInfo.text = alkalineText + item.alkali
            updatePhColor(item)
            updateButtonColor("phenolphthalein_btn")
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun updatePhColor(item: Indicator) {
        try {
            val leftColor = resources.getIdentifier(item.acidColor, "color", packageName)
            val centerColor = resources.getIdentifier(item.neutralColor, "color", packageName)
            val rightColor = resources.getIdentifier(item.alkaliColor, "color", packageName)

            binding.left.setColorFilter(
                ContextCompat.getColor(this, leftColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.center.setColorFilter(
                ContextCompat.getColor(this, centerColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.right.setColorFilter(
                ContextCompat.getColor(this, rightColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables", "DiscouragedApi")
    private fun updateButtonColor(btn: String) {
        binding.phChipBar.methylOrangeBtn.background = getDrawable(R.drawable.shape_chip)
        binding.phChipBar.bromothymolBlueBtn.background = getDrawable(R.drawable.shape_chip)
        binding.phChipBar.congoRedBtn.background = getDrawable(R.drawable.shape_chip)
        binding.phChipBar.phenolphthaleinBtn.background = getDrawable(R.drawable.shape_chip)

        val delay = Handler(Looper.getMainLooper())
        delay.postDelayed({
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            button?.background = getDrawable(R.drawable.shape_chip_active)
        }, 1)
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        val paramsTitle = binding.commonTitleBackPh.layoutParams as ViewGroup.LayoutParams
        paramsTitle.height = top + resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        binding.commonTitleBackPh.layoutParams = paramsTitle

        val pScroll = binding.phScroll.layoutParams as ViewGroup.MarginLayoutParams
        pScroll.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        binding.phScroll.layoutParams = pScroll
    }
}
