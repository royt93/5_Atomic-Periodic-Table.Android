package com.mckimquyen.atomicPeriodicTable.act.setting

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.AInfoBinding
import com.mckimquyen.atomicPeriodicTable.ext.openUrlInBrowser
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref

class AboutAct : BaseAct() {

    // ViewBinding - thay thế Kotlin Synthetics (deprecated)
    private lateinit var binding: AInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        // Apply theme dựa trên user preference
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()

        if (themePrefValue == 100) {
            // Theme tự động theo system
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
            // Light theme
            setTheme(R.style.AppTheme)
        }
        if (themePrefValue == 1) {
            // Dark theme
            setTheme(R.style.AppThemeDark)
        }

        // Inflate ViewBinding và set content view
        binding = AInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ===============================================================
        // Setup Edge-to-Edge & System Bars (Modern API - Android 11+)
        // ===============================================================
        // Thay thế: systemUiVisibility (deprecated)
        // Sử dụng: WindowInsetsControllerCompat (modern, backward compatible)

        // Bật chế độ edge-to-edge: content vẽ dưới status bar & navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Lấy WindowInsetsController để điều khiển system bars
        // WindowInsetsController luôn non-null khi window đã được khởi tạo
        val windowInsetsController = WindowCompat.getInsetsController(window, binding.viewInfo)

        // Ẩn system bars (status bar & navigation bar)
        // Tương đương với: SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Set behavior khi user swipe: system bars sẽ hiện tạm thời rồi tự ẩn
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hiển thị version number từ BuildConfig
        binding.versionNumber.text = "Version ${BuildConfig.VERSION_NAME}"

        // ===============================================================
        // Apply Animations
        // ===============================================================
        // Scale fade in animation cho app icon
        val scaleFadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in)
        binding.imageView3.startAnimation(scaleFadeIn)

        // Fade slide up animation cho hero card
        val fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        binding.heroCard.startAnimation(fadeSlideUp)

        // Fade slide up animation cho source code card với delay nhỏ
        val fadeSlideUpDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        fadeSlideUpDelayed.startOffset = 150 // 150ms delay
        binding.sourceCodeCard.startAnimation(fadeSlideUpDelayed)

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
        binding.backBtn.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }

        // Link tới GitHub repo forked
        binding.btGithubForked.setOnClickListener {
            openUrlInBrowser("https://github.com/gj-loitp/Atomic-Periodic-Table.Android")
        }

        // Link tới GitHub repo gốc
        binding.btGithub.setOnClickListener {
            openUrlInBrowser("https://github.com/JLindemann42/Atomic-Periodic-Table.Android")
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        // Adjust title bar height để tránh system bars (status bar)
        val params = binding.commonTitleBackInfo.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackInfo.layoutParams = params

        // Note: Image margin không cần adjust nữa vì layout mới sử dụng card-based design
        // Icon nằm trong CardView với proper spacing, không cần manual margin adjustment

        // Adjust title box margins để tránh navigation bar (left/right)
        val titleParam = binding.titleBoxInfo.layoutParams as ViewGroup.MarginLayoutParams
        titleParam.rightMargin = right
        titleParam.leftMargin = left
        binding.titleBoxInfo.layoutParams = titleParam
    }
}
