package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.databinding.ASubmitBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils

class SubmitAct : BaseAct() {

    // ViewBinding - thay thế Kotlin Synthetics (deprecated)
    private lateinit var binding: ASubmitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

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
        binding = ASubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup system UI visibility
        binding.viewSub.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // Setup issue type dropdown selector
        dropSelector()

        // Title Controller - hiển thị/ẩn title bar khi scroll
        binding.commonTitleBackSubColor.visibility = View.INVISIBLE
        binding.submitTitle.visibility = View.INVISIBLE
        binding.commonTitleBackSub.elevation = (resources.getDimension(R.dimen.zero_elevation))
        binding.submitScroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 200f
                override fun onScrollChanged() {
                    if (binding.submitScroll.scrollY > 150f) {
                        // Đã scroll xuống -> hiện title bar compact
                        binding.commonTitleBackSubColor.visibility = View.VISIBLE
                        binding.submitTitle.visibility = View.VISIBLE
                        binding.submitTitleDownstate.visibility = View.INVISIBLE
                        binding.commonTitleBackSub.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        // Ở đầu trang → hiện title bar expanded
                        binding.commonTitleBackSubColor.visibility = View.INVISIBLE
                        binding.submitTitle.visibility = View.INVISIBLE
                        binding.submitTitleDownstate.visibility = View.VISIBLE
                        binding.commonTitleBackSub.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = binding.submitScroll.scrollY.toFloat()
                }
            })

        // Back button - quay về màn hình trước
        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        // Adjust title bar height để tránh status bar
        val params = binding.commonTitleBackSub.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackSub.layoutParams = params

        // Adjust expanded title top margin
        val params2 = binding.submitTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        binding.submitTitleDownstate.layoutParams = params2
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Nếu dropdown đang hiển thị -> ẩn dropdown thay vì back
        if (binding.dropIssue.root.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(binding.background, 150)
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
            return
        }
        super.onBackPressed()
    }

    private fun dropSelector() {
        // Mặc định chọn "data_issue"
        var type = "#data_issue"
        buildForm(type)

        // Click dropdown button -> hiện dropdown menu
        binding.dropBtn.setOnClickListener {
            Utils.fadeInAnim(binding.dropIssue.root, 150)
            Utils.fadeInAnim(binding.background, 150)
        }

        // Click background -> ẩn dropdown
        binding.background.setOnClickListener {
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
            Utils.fadeOutAnim(binding.background, 150)
        }

        // Chọn "Data Issue"
        binding.dropIssue.dataIssue.setOnClickListener {
            type = "#data_issue"
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
            Utils.fadeOutAnim(binding.background, 150)
            binding.dropBtn.text = getString(R.string.data_issue)
            buildForm(type)
        }

        // Chọn "Bug"
        binding.dropIssue.bug.setOnClickListener {
            type = "#bug"
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
            Utils.fadeOutAnim(binding.background, 150)
            binding.dropBtn.text = getString(R.string.bug)
            buildForm(type)
        }

        // Chọn "Question"
        binding.dropIssue.question.setOnClickListener {
            type = "#question"
            Utils.fadeOutAnim(binding.dropIssue.root, 150)
            Utils.fadeOutAnim(binding.background, 150)
            binding.dropBtn.text = getString(R.string.question)
            buildForm(type)
        }
    }

    private fun buildForm(type: String) {
        // Submit button - mở email client với pre-filled subject & body
        binding.iBtn.setOnClickListener {
            val title = binding.iTitle.text.toString()
            val content = binding.iContent.text.toString()

            // Tạo email intent với subject và body
            val request = Intent(Intent.ACTION_VIEW)
            request.data = Uri.parse(
                Uri.parse("mailto:roy.mobile.dev@gmail.com?subject=$type $title&body=$content")
                    .toString()
            )
            startActivity(request)
        }
    }
}
