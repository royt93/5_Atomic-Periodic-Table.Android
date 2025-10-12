package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.EquationsAdt
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.model.Equation
import com.mckimquyen.atomicPeriodicTable.model.EquationModel
import com.mckimquyen.atomicPeriodicTable.databinding.AEquationsBinding
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils
import java.util.Locale

class EquationsAct : BaseAct(), EquationsAdt.OnEquationClickListener {
    private lateinit var binding: AEquationsBinding
    private var equationList = ArrayList<Equation>()
    private var mAdapter = EquationsAdt(list = equationList, clickListener = this, context = this)

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
        binding = AEquationsBinding.inflate(layoutInflater)
        setContentView(binding.root) //REMEMBER: Never move any function calls above this

        recyclerView()
        clickSearch()
        binding.eInc.eBackBtn.setOnClickListener { hideInfoPanel() }
        binding.eInc.lBackgroundE.setOnClickListener { hideInfoPanel() }

        binding.viewEqu.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        binding.backBtnEqu.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        binding.equRecycler.setPadding(
            /* left = */ 0,
            /* top = */
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            /* right = */
            0,
            /* bottom = */
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        val params2 = binding.commonTitleBackEqu.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackEqu.layoutParams = params2

        val searchEmptyImgPrm = binding.emptySearchBoxEqu.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.emptySearchBoxEqu.layoutParams = searchEmptyImgPrm
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recyclerView() {
        val equation = ArrayList<Equation>()

        EquationModel.getList(equation)
        binding.equRecycler.layoutManager = LinearLayoutManager(
            /* context = */ this,
            /* orientation = */ RecyclerView.VERTICAL,
            /* reverseLayout = */ false
        )
        val adapter = EquationsAdt(list = equation, clickListener = this, context = this)
        binding.equRecycler.adapter = adapter

        equation.sortWith { lhs, rhs ->
            if (lhs.equationTitle < rhs.equationTitle) -1 else if (lhs.equationTitle < rhs.equationTitle) 1 else 0
        }

        adapter.notifyDataSetChanged()

        binding.editEqu.addTextChangedListener(object : TextWatcher {
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
                filter(s.toString(), equation, binding.equRecycler)
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.eInc.root.visibility == View.VISIBLE) {
            hideInfoPanel()
            return
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String, list: ArrayList<Equation>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Equation> = ArrayList()
        for (item in list) {
            if (item.equationTitle.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (recyclerView.adapter?.itemCount == 0) {
                Anim.fadeIn(binding.emptySearchBoxEqu, 300)
            } else {
                binding.emptySearchBoxEqu.visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = EquationsAdt(filteredList, this, this)
    }

    private fun clickSearch() {
        binding.searchBtnEqu.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarEqu, 150)
            Utils.fadeOutAnim(binding.titleBoxEqu, 1)

            binding.editEqu.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editEqu, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeEquSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarEqu, 1)

            val delayClose = Handler(Looper.getMainLooper())
            delayClose.postDelayed({
                Utils.fadeInAnim(binding.titleBoxEqu, 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    override fun equationClickListener(item: Equation, position: Int) {
        showInfoPanel(title = item.equation, text = item.description)
    }

    private fun showInfoPanel(title: Int, text: String) {
        Anim.fadeIn(binding.eInc.root, 150)

        binding.eInc.eTitle.setImageResource(title)
        val themePref = ThemePref(this)
        val themePrefValue = themePref.getValue()
        if (themePrefValue == 1) {
            binding.eInc.eTitle.colorFilter = ColorMatrixColorFilter(negative)
        }
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.eInc.eTitle.colorFilter = ColorMatrixColorFilter(negative)
                }
            }
        }
        binding.eInc.eText.text = text
    }

    private fun hideInfoPanel() {
        Anim.fadeOutAnim(view = binding.eInc.root, time = 150)
    }

    private val negative = floatArrayOf(
        -1.0f, 0f, 0f, 0f, 255f,
        0f, -1.0f, 0f, 0f, 255f,
        0f, 0f, -1.0f, 0f, 255f,
        0f, 0f, 0f, 1.0f, 0f
    )
}
