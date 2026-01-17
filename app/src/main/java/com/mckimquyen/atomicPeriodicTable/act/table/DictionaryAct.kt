package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.DictionaryAdt
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.model.Dictionary
import com.mckimquyen.atomicPeriodicTable.databinding.ADictionaryBinding
import com.mckimquyen.atomicPeriodicTable.model.DictionaryModel
import com.mckimquyen.atomicPeriodicTable.pref.DictionaryPref
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils
import java.util.Locale

class DictionaryAct : BaseAct(), DictionaryAdt.OnDictionaryClickListener {
    private lateinit var binding: ADictionaryBinding
    private var dictionaryList = ArrayList<Dictionary>()
    private var mAdapter = DictionaryAdt(
        dictionaryList = dictionaryList,
        clickListener = this,
        con = this
    )

    // Handler instances for memory leak prevention
    private var updateButtonHandler: Handler? = null
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
        binding = ADictionaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.rcView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val itemse = ArrayList<Dictionary>()
        DictionaryModel.getList(this, itemse)

        recyclerView()
        clickSearch()
        chipListeners()
        binding.clearBtn.visibility = View.GONE

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
        binding.backBtnD.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun chipListeners() {
        // Setup chip button listeners cho category selection
        binding.chemistryBtn.setOnClickListener {
            updateButtonColor("chemistry_btn")
            val dictionaryPreference = DictionaryPref(this)
            dictionaryPreference.setValue("chemistry")
            binding.editIso.setText("test")
            binding.editIso.setText("")
        }
        binding.physicsBtn.setOnClickListener {
            updateButtonColor("physics_btn")
            val dictionaryPreference = DictionaryPref(this)
            dictionaryPreference.setValue("physics")
            binding.editIso.setText("test1")
            binding.editIso.setText("")
        }
        binding.mathBtn.setOnClickListener {
            updateButtonColor("math_btn")
            val dictionaryPreference = DictionaryPref(this)
            dictionaryPreference.setValue("math")
            binding.editIso.setText("test1")
            binding.editIso.setText("")
        }
        binding.reactionsBtn.setOnClickListener {
            updateButtonColor("reactions_btn")
            val dictionaryPreference = DictionaryPref(this)
            dictionaryPreference.setValue("reactions")
            binding.editIso.setText("test1")
            binding.editIso.setText("")
        }
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables", "DiscouragedApi")
    private fun updateButtonColor(btn: String) {
        binding.chemistryBtn.background = getDrawable(R.drawable.shape_chip)
        binding.physicsBtn.background = getDrawable(R.drawable.shape_chip)
        binding.mathBtn.background = getDrawable(R.drawable.shape_chip)
        binding.reactionsBtn.background = getDrawable(R.drawable.shape_chip)

        updateButtonHandler = Handler(Looper.getMainLooper())
        updateButtonHandler?.postDelayed({
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            button?.background = getDrawable(R.drawable.shape_chip_active)
        }, 200)

        binding.clearBtn.visibility = View.VISIBLE
        binding.clearBtn.setOnClickListener {
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            val dictionaryPreference = DictionaryPref(this)
            button?.background = getDrawable(R.drawable.shape_chip)
            dictionaryPreference.setValue("")
            binding.editIso.setText("test1")
            binding.editIso.setText("")
            binding.clearBtn.visibility = View.GONE
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        binding.rcView.setPadding(
            /* left = */ 0,
            /* top = */ resources.getDimensionPixelSize(R.dimen.title_bar_ph) + top,
            /* right = */ 0,
            /* bottom = */ resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        )
        val params2 = binding.commonTitleBackDic.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        binding.commonTitleBackDic.layoutParams = params2

        val searchEmptyImgPrm = binding.emptySearchBoxDic.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.emptySearchBoxDic.layoutParams = searchEmptyImgPrm
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recyclerView() {
        val recyclerView = binding.rcView
        val dictionaryList = ArrayList<Dictionary>()
        DictionaryModel.getList(this, dictionaryList)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = DictionaryAdt(dictionaryList = dictionaryList, clickListener = this, con = this)
        recyclerView.adapter = adapter
        // Sắp xếp danh sách dictionary theo heading (alphabetically)
        dictionaryList.sortWith { lhs, rhs ->
            if (lhs.heading < rhs.heading) -1 else if (lhs.heading > rhs.heading) 1 else 0
        }

        adapter.notifyDataSetChanged()
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), dictionaryList, recyclerView)
            }
        }
        binding.editIso.addTextChangedListener(textWatcher)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(
        text: String,
        list: ArrayList<Dictionary>,
        recyclerView: RecyclerView,
    ) {
        val filteredList: ArrayList<Dictionary> = ArrayList()
        for (item in list) {
            val dictionaryPreference = DictionaryPref(this)
            val dictionaryPrefValue1 = dictionaryPreference.getValue()
            if (item.heading.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                if (item.category.lowercase(Locale.ROOT)
                        .contains(dictionaryPrefValue1.lowercase(Locale.ROOT))
                ) {
                    filteredList.add(item)
                }
            }
            filterHandler = Handler(Looper.getMainLooper())
            filterHandler?.postDelayed({
                if (recyclerView.adapter?.itemCount == 0) {
                    Anim.fadeIn(binding.emptySearchBoxDic, 300)
                } else {
                    binding.emptySearchBoxDic.visibility = View.GONE
                }
            }, 10)
            mAdapter.notifyDataSetChanged()
            mAdapter.filterList(filteredList)
            recyclerView.adapter = DictionaryAdt(
                dictionaryList = filteredList,
                clickListener = this,
                con = this
            )
        }
    }

    private fun clickSearch() {
        binding.searchBtn.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarIso, 150)
            Utils.fadeOutAnim(binding.titleBox, 1)

            binding.editIso.requestFocus()
            // Hiện keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editIso, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeIsoSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarIso, 1)

            // Handler với Looper để delay hiện lại title box
            delayCloseHandler = Handler(Looper.getMainLooper())
            delayCloseHandler?.postDelayed({
                Utils.fadeInAnim(binding.titleBox, 150)
            }, 151)

            // Ẩn keyboard
            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    override fun dictionaryClickListener(
        item: Dictionary,
        wiki: TextView,
        url: String,
        position: Int,
    ) {
        val packageNameString = "com.android.chrome"

        // Modern Custom Tabs API - sử dụng CustomTabColorSchemeParams
        val colorSchemeParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.wikipediaColor))
            .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.wikipediaColor))
            .build()

        val customTabBuilder = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorSchemeParams)
            .setShowTitle(true)

        val customTab = customTabBuilder.build()
        val intent = customTab.intent
        // Sử dụng toUri() extension thay vì Uri.parse()
        intent.data = url.toUri()

        val packageManager = packageManager
        val resolveInfoList = packageManager.queryIntentActivities(
            customTab.intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            if (TextUtils.equals(packageName, packageNameString))
                customTab.intent.setPackage(packageNameString)
        }
        customTab.intent.data?.let { it1 -> customTab.launchUrl(this, it1) }
    }

    override fun onDestroy() {
        // Clean up handlers to prevent memory leaks
        updateButtonHandler?.removeCallbacksAndMessages(null)
        updateButtonHandler = null
        filterHandler?.removeCallbacksAndMessages(null)
        filterHandler = null
        delayCloseHandler?.removeCallbacksAndMessages(null)
        delayCloseHandler = null

        // Clean up text watcher to prevent memory leaks
        textWatcher?.let {
            binding.editIso.removeTextChangedListener(it)
        }
        textWatcher = null

        super.onDestroy()
    }
}
