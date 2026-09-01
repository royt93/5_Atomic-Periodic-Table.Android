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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.IonAdapter
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.databinding.AIonBinding
import com.mckimquyen.atomicPeriodicTable.model.Ion
import com.mckimquyen.atomicPeriodicTable.model.IonModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale

class IonAct : BaseAct(), IonAdapter.OnIonClickListener {
    private lateinit var binding: AIonBinding

    // FIX-008: this used to be a second, never-attached IonAdapter instance built from an
    // always-empty list — filter() called filterList()/notifyDataSetChanged() on it
    // pointlessly, then discarded the REAL attached adapter by replacing
    // recyclerView.adapter with yet another new instance every keystroke (losing scroll
    // position). Now there is exactly one adapter instance, created once in
    // recyclerView() and reused by filter().
    private lateinit var mAdapter: IonAdapter

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
        binding = AIonBinding.inflate(layoutInflater)
        setContentView(binding.root) //REMEMBER: Never move any function calls above this

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

        // Click listener cho background của ion detail panel để ẩn panel
        binding.ionDetail.tvDetailBackgroundIon.setOnClickListener {
            Utils.fadeOutAnim(binding.ionDetail.root, 300)
        }

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press
        // Logic đặc biệt: Nếu ion detail panel đang hiển thị -> ẩn panel thay vì back
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kiểm tra xem ion detail panel có đang hiển thị không
                if (binding.ionDetail.root.isVisible) {
                    // Panel đang hiện -> ẩn panel với fade animation thay vì finish activity
                    Utils.fadeOutAnim(binding.ionDetail.root, 300)
                } else {
                    // Panel đã ẩn -> finish activity và quay về màn hình trước
                    finish()
                }
            }
        })

        // Click listener cho nút back trên UI
        binding.backBtnIon.setOnClickListener {
            // Trigger back press event qua dispatcher
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n", "DiscouragedApi")
    override fun ionClickListener(item: Ion, position: Int) {
        if (item.count > 1) {
            Utils.fadeInAnim(binding.ionDetail.root, 300)
            binding.ionDetail.ionDetailTitle.text = ((item.name).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            } + " " + "ionization")
            var jsonString: String?
            val ext = ".json"
            val element = item.name
            val elementJson = "$element$ext"
            val inputStream: InputStream = assets.open(elementJson)
            jsonString = inputStream.bufferedReader().use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            for (i in 1..item.count) {
                val text = "element_ionization_energy"
                val add = i.toString()
                val final = (text + add)
                val ionization = jsonObject.optString(final, "---")
                val extText = i.toString()
                val name = "ion_text_"
                val eView = "$name$extText"
                val iText =
                    findViewById<TextView>(resources.getIdentifier(eView, "id", packageName))
                val dot = "."
                val space = " "
                iText?.text = "$i$dot$space$ionization"
                iText?.visibility = View.VISIBLE
            }
            for (i in (item.count + 1)..30) {
                val extText = i.toString()
                val name = ("ion_text_")
                val eView = "$name$extText"
                val iText =
                    findViewById<TextView>(resources.getIdentifier(eView, "id", packageName))
                iText?.visibility = View.GONE
            }
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        binding.ionView.setPadding(
            /* left = */ 0,
            /* top = */
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            /* right = */
            0,
            /* bottom = */
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )
        val params2 = binding.commonTitleBackIon.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackIon.layoutParams = params2

        val searchEmptyImgPrm = binding.emptySearchBoxIon.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.emptySearchBoxIon.layoutParams = searchEmptyImgPrm
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recyclerView() {
        val ionList = ArrayList<Ion>()
        IonModel.getList(ionList)
        binding.ionView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter = IonAdapter(ionList, this, this)
        binding.ionView.adapter = mAdapter

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), ionList, binding.ionView)
            }
        }
        binding.editIon.addTextChangedListener(textWatcher)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String, list: ArrayList<Ion>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Ion> = ArrayList()
        for (item in list) {
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        filterHandler = Handler(Looper.getMainLooper())
        filterHandler?.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(binding.emptySearchBoxIon, 300)
            } else {
                binding.emptySearchBoxIon.visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
    }

    private fun clickSearch() {
        binding.searchBtnIon.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarIon, 150)
            Utils.fadeOutAnim(binding.titleBox, 1)
            binding.editIon.requestFocus()
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editIon, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeEleSearchIon.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarIon, 1)
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
            binding.editIon.removeTextChangedListener(it)
        }
        textWatcher = null

        super.onDestroy()
    }

}
