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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.adt.IonAdapter
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.model.Ion
import com.mckimquyen.atomicPeriodicTable.databinding.AIonBinding
import com.mckimquyen.atomicPeriodicTable.model.IonModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.util.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale

class IonAct : BaseAct(), IonAdapter.OnIonClickListener {
    private lateinit var binding: AIonBinding
    private var ionList = ArrayList<Ion>()
    private var mAdapter = IonAdapter(list = ionList, clickListener = this, context = this)

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
        binding.viewIon.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        binding.ionDetail.tvDetailBackgroundIon.setOnClickListener {
            Utils.fadeOutAnim(binding.ionDetail.root, 300)
        }
        binding.backBtnIon.setOnClickListener {
            this.onBackPressed()
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
            var jsonString: String? = null
            val ext = ".json"
            val element = item.name
            val elementJson = "$element$ext"
            val inputStream: InputStream = assets.open(elementJson.toString())
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
        val adapter = IonAdapter(ionList, this, this)
        binding.ionView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.editIon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), ionList, binding.ionView)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String, list: ArrayList<Ion>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Ion> = ArrayList()
        for (item in list) {
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(binding.emptySearchBoxIon, 300)
            } else {
                binding.emptySearchBoxIon.visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = IonAdapter(list = filteredList, clickListener = this, context = this)
    }

    private fun clickSearch() {
        binding.searchBtnIon.setOnClickListener {
            Utils.fadeInAnim(binding.searchBarIon, 150)
            Utils.fadeOutAnim(binding.titleBox, 1)
            binding.editIon.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editIon, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.closeEleSearchIon.setOnClickListener {
            Utils.fadeOutAnim(binding.searchBarIon, 1)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.ionDetail.root.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(binding.ionDetail.root, 300)
            return
        } else {
            super.onBackPressed()
        }
    }

}
