package com.mckimquyen.atomicPeriodicTable.act.table

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
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

        binding.viewEle.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
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
        binding.eView.setPadding(
            /* left = */ 0,
            /* top = */ resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            /* right = */ 0,
            /* bottom = */ resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        val params2 = binding.commonTitleBackElo.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackElo.layoutParams = params2

        val searchEmptyImgPrm = binding.emptySearchBoxEle.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.emptySearchBoxEle.layoutParams = searchEmptyImgPrm
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recyclerView() {
        val recyclerView = binding.eView
        val series = ArrayList<Series>()

        SeriesModel.getList(series)
        recyclerView.layoutManager = LinearLayoutManager(
            /* context = */ this,
            /* orientation = */ RecyclerView.VERTICAL,
            /* reverseLayout = */ false
        )
        val adapter = ElectrodeAdt(list = series, clickListener = this, context = this)
        recyclerView.adapter = adapter

        adapter.notifyDataSetChanged()

        binding.editEle.addTextChangedListener(object : TextWatcher {
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
        })
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
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
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
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editEle, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeEleSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarEle, 1)

            val delayClose = Handler(Looper.getMainLooper())
            delayClose.postDelayed({
                Utils.fadeInAnim(binding.titleBox, 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }


}
