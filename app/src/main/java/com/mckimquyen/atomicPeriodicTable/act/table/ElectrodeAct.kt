package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.ElectrodeAdt
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.databinding.AElectrodeBinding
import com.mckimquyen.atomicPeriodicTable.model.Series
import com.mckimquyen.atomicPeriodicTable.model.SeriesModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils
import java.util.Locale

class ElectrodeAct : BaseAct() {
    private lateinit var binding: AElectrodeBinding
    private var seriesList = ArrayList<Series>()
    private var mAdapter = ElectrodeAdt(list = seriesList, clickListener = this, context = this)

    // Handler instances for memory leak prevention
    private var filterHandler: Handler? = null
    private var delayCloseHandler: Handler? = null
    private var textWatcher: TextWatcher? = null

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
        binding = AElectrodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView()
        clickSearch()

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
        binding.backBtn.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        // Set padding for RecyclerView to account for title bar (same as EquationsAct, IonAct)
        binding.eView.setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        // Adjust title bar height to include status bar
        val params2 = binding.commonTitleBackElo.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackElo.layoutParams = params2

        // Adjust empty search box margin
        val searchEmptyImgPrm = binding.emptySearchBoxEle.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.emptySearchBoxEle.layoutParams = searchEmptyImgPrm
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recyclerView() {
        val recyclerView = binding.eView
        val series = ArrayList<Series>()

        SeriesModel.getList(series)
        recyclerView.layoutManager = LinearLayoutManager(/* context = */ this,/* orientation = */
            RecyclerView.VERTICAL,/* reverseLayout = */
            false
        )
        val adapter = ElectrodeAdt(list = series, clickListener = this, context = this)
        recyclerView.adapter = adapter

        adapter.notifyDataSetChanged()

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int,
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                filter(text = s.toString(), list = series, recyclerView = recyclerView)
            }
        }
        binding.editEle.addTextChangedListener(textWatcher)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(
        text: String,
        list: ArrayList<Series>,
        recyclerView: RecyclerView,
    ) {
        val filteredList: ArrayList<Series> = ArrayList()
        for (item in list) {
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        filterHandler = Handler(Looper.getMainLooper())
        filterHandler?.postDelayed({
            if (recyclerView.adapter?.itemCount == 0) {
                Anim.fadeIn(binding.emptySearchBoxEle, 300)
            } else {
                binding.emptySearchBoxEle.visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = ElectrodeAdt(filteredList, this, this)
    }

    private fun clickSearch() {
        binding.searchBtn.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarEle, 150)
            Utils.fadeOutAnim(binding.titleBox, 1)

            binding.editEle.requestFocus()
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editEle, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeEleSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarEle, 1)

            delayCloseHandler = Handler(Looper.getMainLooper())
            delayCloseHandler?.postDelayed({
                Utils.fadeInAnim(binding.titleBox, 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    override fun onDestroy() {
        // Clean up handlers to prevent memory leaks
        filterHandler?.removeCallbacksAndMessages(null)
        filterHandler = null
        delayCloseHandler?.removeCallbacksAndMessages(null)
        delayCloseHandler = null

        // Clean up text watcher to prevent memory leaks
        textWatcher?.let {
            binding.editEle.removeTextChangedListener(it)
        }
        textWatcher = null

        super.onDestroy()
    }

}
