package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.adt.IsotopeAdt
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.IsoPref
import com.mckimquyen.atomicPeriodicTable.pref.SendIso
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.databinding.AIsotopesExperimentalBinding
import com.mckimquyen.atomicPeriodicTable.util.ToastUtil
import com.mckimquyen.atomicPeriodicTable.util.Utils
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class IsotopesActExperimental : BaseAct(), IsotopeAdt.OnElementClickListener {
    private lateinit var binding: AIsotopesExperimentalBinding
    private var elementList = ArrayList<Element>()
    var mAdapter = IsotopeAdt(elementList = elementList, clickListener = this, context = this)

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
        binding = AIsotopesExperimentalBinding.inflate(layoutInflater)
        setContentView(binding.root) //Don't move down (Needs to be before we call our functions)

        binding.slidPanel.slidingLayoutI.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        binding.rView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val adapter = IsotopeAdt(elementList = elements, clickListener = this, context = this)
        binding.rView.adapter = adapter

        binding.editIso.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), elements, binding.rView)
            }
        })

        binding.slidPanel.slidingLayoutI.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {}
            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState,
                newState: SlidingUpPanelLayout.PanelState,
            ) {
                if (binding.slidPanel.slidingLayoutI.panelState === SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    Utils.fadeOutAnim(binding.backgroundI2, 300)
                    Utils.fadeOutAnim(binding.slidPanel.root, 300)
                }
            }
        })

        binding.backgroundI2.setOnClickListener {
            if (binding.panelInfo.root.visibility == View.VISIBLE) {
                Utils.fadeOutAnim(binding.panelInfo.root, 300)
                Utils.fadeOutAnim(binding.backgroundI2, 300)
            } else {
                Utils.fadeOutAnim(binding.slidPanel.slidingLayoutI, 300)
                Utils.fadeOutAnim(binding.backgroundI2, 300)
            }
        }

        binding.view1.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        clickSearch()
        searchFilter(elements, binding.rView)
        sentIsotope()
        binding.backBtn.setOnClickListener {
            this.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchFilter(list: ArrayList<Element>, recyclerView: RecyclerView) {
        binding.filterBtn2.setOnClickListener {
            Utils.fadeInAnim(binding.isoFilterBox.root, 150)
            Utils.fadeInAnim(binding.filterBackground, 150)
        }
        binding.filterBackground.setOnClickListener {
            Utils.fadeOutAnim(binding.isoFilterBox.root, 150)
            Utils.fadeOutAnim(binding.filterBackground, 150)
        }
        binding.isoFilterBox.isoAlphabetBtn.setOnClickListener {
            val isoPreference = IsoPref(this)
            isoPreference.setValue(0)

            val filtList: ArrayList<Element> = ArrayList()
            for (item in list) {
                filtList.add(item)
            }
            Utils.fadeOutAnim(binding.isoFilterBox.root, 150)
            Utils.fadeOutAnim(binding.filterBackground, 150)
            filtList.sortWith { lhs, rhs ->
                if (lhs.element < rhs.element) -1 else if (lhs.element < rhs.element) 1 else 0
            }
            mAdapter.filterList(filtList)
            mAdapter.notifyDataSetChanged()
            recyclerView.adapter = IsotopeAdt(
                elementList = filtList,
                clickListener = this,
                context = this
            )
        }
        binding.isoFilterBox.isoElementNumbBtn.setOnClickListener {
            val isoPreference = IsoPref(this)
            isoPreference.setValue(1)

            val filtList: ArrayList<Element> = ArrayList()
            for (item in list) {
                filtList.add(item)
            }
            Utils.fadeOutAnim(binding.isoFilterBox.root, 150)
            Utils.fadeOutAnim(binding.filterBackground, 150)
            mAdapter.filterList(filtList)
            mAdapter.notifyDataSetChanged()
            recyclerView.adapter = IsotopeAdt(
                elementList = filtList,
                clickListener = this,
                context = this
            )
        }
    }

    private fun clickSearch() {
        binding.searchBtn.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarIso, 300)
            Utils.fadeOutAnim(binding.titleBox, 300)

            binding.editIso.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editIso, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeIsoSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarIso, 300)
            Utils.fadeInAnim(binding.titleBox, 300)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String, list: ArrayList<Element>, recyclerView: RecyclerView) {
        val isoPreference = IsoPref(this)
        val isoPrefValue = isoPreference.getValue()
        val filteredList: ArrayList<Element> = ArrayList()
        for (item in list) {
            if (item.element.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
                Log.v("SSDD2", filteredList.toString())
            }
        }
        if (isoPrefValue == 0) {
            filteredList.sortWith { lhs, rhs ->
                if (lhs.element < rhs.element) -1 else if (lhs.element < rhs.element) 1 else 0
            }
        }
        val handler = android.os.Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (recyclerView.adapter?.itemCount == 0) {
                Anim.fadeIn(binding.emptySearchBoxIso, 300)
            } else {
                binding.emptySearchBoxIso.visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = IsotopeAdt(
            elementList = filteredList,
            clickListener = this,
            context = this
        )
    }

    override fun elementClickListener(item: Element, position: Int) {
        val elementSendAndLoad = ElementSendAndLoad(this)
        elementSendAndLoad.setValue(item.element)
        drawCard(elementList)

        Utils.fadeInAnimBack(binding.backgroundI2, 300)
        Utils.fadeInAnim(binding.slidPanel.root, 300)
        binding.slidPanel.slidingLayoutI.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    private fun sentIsotope() {
        val isoSent = SendIso(this)
        if (isoSent.getValue() == "true") {
            drawCard(elementList)
            Utils.fadeInAnimBack(binding.backgroundI2, 300)
            Utils.fadeInAnim(binding.slidPanel.root, 300)
            binding.slidPanel.slidingLayoutI.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            isoSent.setValue("false")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.backgroundI2.visibility == View.VISIBLE) {
            binding.slidPanel.slidingLayoutI.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (binding.filterBackground.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(binding.filterBackground, 150)
            Utils.fadeOutAnim(binding.isoFilterBox.root, 150)
            return
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun drawCard(list: ArrayList<Element>) {
        ElementModel.getList(list)
        var jsonString: String?
        for (item in list) {
            try {
                val elementSendLoad = ElementSendAndLoad(this)
                val nameVal = elementSendLoad.getValue()
                if (item.element.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } == nameVal?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }) {
                    val ext = ".json"
                    val elementJson = "$nameVal$ext"
                    val inputStream: InputStream = assets.open(elementJson)
                    jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)
                    val jsonObject: JSONObject = jsonArray.getJSONObject(0)

                    binding.slidPanel.frameIso.removeAllViews()

                    val aLayout = binding.slidPanel.frameIso
                    val inflater = layoutInflater
                    val fLayout: View =
                        inflater.inflate(R.layout.view_row_iso_panel_title_item, aLayout, false)

                    val iTitle = fLayout.findViewById(R.id.tvIsoTitle) as TextView
                    val iExt = " Isotopes"
                    iTitle.text = "${
                        nameVal.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                    }$iExt"

                    aLayout.addView(fLayout)

                    for (i in 1..item.isotopes) {
                        val mainLayout = binding.slidPanel.frameIso
                        val inflater = layoutInflater
                        val myLayout: View = inflater.inflate(R.layout.view_row_iso_panel_item, mainLayout, false)
                        val name = "iso_"
                        val z = "iso_Z_"
                        val n = "iso_N_"
                        val a = "iso_A_"
                        val half = "iso_half_"
                        val mass = "iso_mass_"
                        val halfText = "Half-Time: "
                        val massText = "Mass: "

                        val isoName = jsonObject.optString("$name$i", "---")
                        val isoZ = jsonObject.optString("$z$i", "---")
                        val isoN = jsonObject.optString("$n$i", "---")
                        val isoA = jsonObject.optString("$a$i", "---")
                        val isoHalf = jsonObject.optString("$half$i", "---")
                        val isoMass = jsonObject.optString("$mass$i", "---")

                        val iName = myLayout.findViewById(R.id.tvIName) as TextView
                        val iZ = myLayout.findViewById(R.id.tviIZ) as TextView
                        val iN = myLayout.findViewById(R.id.tvIN) as TextView
                        val iA = myLayout.findViewById(R.id.tvIA) as TextView
                        val iHalf = myLayout.findViewById(R.id.tvIHalf) as TextView
                        val iMass = myLayout.findViewById(R.id.tvIMass) as TextView

                        iName.text = isoName
                        iZ.text = isoZ
                        iN.text = isoN
                        iA.text = isoA
                        iHalf.text = "$halfText$isoHalf"
                        iMass.text = "$massText$isoMass"

                        mainLayout.addView(myLayout)
                    }
                }
            } catch (e: IOException) {
                ToastUtil.showToast(this, "Couldn't load Data")
            }
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        binding.rView.setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )
        val params2 = binding.commonTitleBackIso.layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.commonTitleBackIso.layoutParams = params2

        val params3 = binding.slidPanel.slidingLayoutI.layoutParams as ViewGroup.MarginLayoutParams
        params3.topMargin = top + resources.getDimensionPixelSize(R.dimen.panel_margin)
        binding.slidPanel.slidingLayoutI.layoutParams = params3

        val searchEmptyImgPrm = binding.emptySearchBoxIso.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.emptySearchBoxIso.layoutParams = searchEmptyImgPrm
    }
}
