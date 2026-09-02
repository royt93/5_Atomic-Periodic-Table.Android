package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.databinding.ACompareBinding
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale

class CompareAct : BaseAct() {

    private lateinit var binding: ACompareBinding

    // Current selections
    private var element1Name: String = "hydrogen"
    private var element2Name: String = "helium"

    // Element C (mục 16: 3-way compare) — null means the 3rd column is not active (default,
    // matches the original 2-element experience exactly).
    private var element3Name: String? = null

    // Memory leak prevention: store TextWatcher references for cleanup
    private var textWatcher1: TextWatcher? = null
    private var textWatcher2: TextWatcher? = null
    private var textWatcher3: TextWatcher? = null

    // Memory leak prevention: store the delayed-hide handler for focus-loss dropdowns
    // postDelayed on a View is NOT auto-cancelled when Activity is destroyed → must cancel manually
    private var focusHideHandler: android.os.Handler? = null

    // Memory leak prevention: store the filtered list for search
    private var allElements: ArrayList<Element> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }


    private fun setupViews() {
        binding = ACompareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Load element list once
        ElementModel.getList(allElements)

        // Read elements passed via Intent extras (from ElementInfoAct "Compare" button)
        element1Name = intent.getStringExtra(EXTRA_ELEMENT_1) ?: "hydrogen"
        element2Name = intent.getStringExtra(EXTRA_ELEMENT_2) ?: "helium"

        setupSearchPickers()
        updateComparisonTable()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        binding.compareBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ================================================================
    // Search Pickers for Element 1 and Element 2
    // ================================================================
    private fun setupSearchPickers() {
        // Set initial labels
        updatePickerLabel(binding.tvElement1Label, element1Name)
        updatePickerLabel(binding.tvElement2Label, element2Name)

        // TextWatcher for element 1 search input
        textWatcher1 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filterPickerDropdown(
                    query = s.toString(),
                    dropdownView = binding.dropdownElement1,
                    onSelect = { name ->
                        element1Name = name
                        updatePickerLabel(binding.tvElement1Label, name)
                        binding.searchElement1.setText("")
                        binding.dropdownElement1.visibility = View.GONE
                        updateComparisonTable()
                    }
                )
            }
        }
        binding.searchElement1.addTextChangedListener(textWatcher1)

        // TextWatcher for element 2 search input
        textWatcher2 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filterPickerDropdown(
                    query = s.toString(),
                    dropdownView = binding.dropdownElement2,
                    onSelect = { name ->
                        element2Name = name
                        updatePickerLabel(binding.tvElement2Label, name)
                        binding.searchElement2.setText("")
                        binding.dropdownElement2.visibility = View.GONE
                        updateComparisonTable()
                    }
                )
            }
        }
        binding.searchElement2.addTextChangedListener(textWatcher2)

        // TextWatcher for element 3 search input (mục 16: 3-way compare)
        textWatcher3 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filterPickerDropdown(
                    query = s.toString(),
                    dropdownView = binding.dropdownElement3,
                    onSelect = { name ->
                        element3Name = name
                        updatePickerLabel(binding.tvElement3Label, name)
                        binding.searchElement3.setText("")
                        binding.dropdownElement3.visibility = View.GONE
                        updateComparisonTable()
                    }
                )
            }
        }
        binding.searchElement3.addTextChangedListener(textWatcher3)

        binding.compareToggleThirdBtn.setOnClickListener {
            toggleThirdElement()
        }

        // Race condition fix: Hide dropdown on focus loss with a small delay.
        // Without delay, focus leaves EditText BEFORE the dropdown item click is processed,
        // causing the dropdown to disappear and the click to be lost (items become unclickable).
        // Memory leak fix: use focusHideHandler (cancelled in onDestroy) instead of View.postDelayed
        // which is NOT auto-cancelled when Activity is destroyed and could run on a dead view.
        focusHideHandler = android.os.Handler(android.os.Looper.getMainLooper())
        binding.searchElement1.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                focusHideHandler?.postDelayed({
                    if (!isDestroyed) binding.dropdownElement1.visibility = View.GONE
                }, 150)
            }
        }
        binding.searchElement2.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                focusHideHandler?.postDelayed({
                    if (!isDestroyed) binding.dropdownElement2.visibility = View.GONE
                }, 150)
            }
        }
        binding.searchElement3.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                focusHideHandler?.postDelayed({
                    if (!isDestroyed) binding.dropdownElement3.visibility = View.GONE
                }, 150)
            }
        }
    }

    // ================================================================
    // Toggle a 3rd element column on/off (mục 16: 3-way compare, v1 scope)
    // ================================================================
    private fun toggleThirdElement() {
        if (element3Name == null) {
            element3Name = DEFAULT_THIRD_ELEMENT
            updatePickerLabel(binding.tvElement3Label, DEFAULT_THIRD_ELEMENT)
            binding.compareVsSpacer2.visibility = View.VISIBLE
            binding.cardElement3.visibility = View.VISIBLE
            binding.tvHeaderElement3.visibility = View.VISIBLE
            binding.compareToggleThirdBtn.text = getString(R.string.compare_remove_third_element)
        } else {
            element3Name = null
            binding.compareVsSpacer2.visibility = View.GONE
            binding.cardElement3.visibility = View.GONE
            binding.tvHeaderElement3.visibility = View.GONE
            binding.compareToggleThirdBtn.text = getString(R.string.compare_add_third_element)
        }
        updateComparisonTable()
    }

    // ================================================================
    // Dynamic dropdown: inflates TextViews dynamically for matched elements
    // ================================================================
    private fun filterPickerDropdown(
        query: String,
        dropdownView: ViewGroup,
        onSelect: (String) -> Unit,
    ) {
        dropdownView.removeAllViews()

        if (query.isBlank()) {
            dropdownView.visibility = View.GONE
            return
        }

        val filtered = allElements.filter {
            it.element.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
                    it.short.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
        }.take(6) // Limit to 6 suggestions to avoid long list

        if (filtered.isEmpty()) {
            dropdownView.visibility = View.GONE
            return
        }

        dropdownView.visibility = View.VISIBLE

        filtered.forEach { element ->
            val tv = TextView(this).apply {
                val displayName = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(
                    this@CompareAct,
                    element.element
                )
                text = "${element.short}  –  $displayName"
                textSize = 14f
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.margin),
                    resources.getDimensionPixelSize(R.dimen.margin_space_card),
                    resources.getDimensionPixelSize(R.dimen.margin),
                    resources.getDimensionPixelSize(R.dimen.margin_space_card)
                )
                val textColor = com.google.android.material.color.MaterialColors.getColor(
                    this@CompareAct,
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK
                )
                setTextColor(textColor)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onSelect(element.element)
                }
            }
            dropdownView.addView(tv)

            // Divider view between items (except last) — theme-aware for light/dark mode
            if (element != filtered.last()) {
                // Bug fix: use this@CompareAct (Activity context) not 'this' (View context)
                // MaterialColors.getColor() requires a View or Context — passing View 'this' inside apply{}
                // would resolve to the View itself which has no theme attrs, causing wrong color fallback.
                val activityContext = this@CompareAct
                val divider = View(activityContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                    val outlineColor = com.google.android.material.color.MaterialColors.getColor(
                        activityContext,
                        com.google.android.material.R.attr.colorOutline,
                        Color.GRAY
                    )
                    setBackgroundColor(outlineColor)
                    alpha = 0.4f
                }
                dropdownView.addView(divider)
            }
        }
    }

    private fun updatePickerLabel(tv: TextView, elementName: String) {
        val display = com.mckimquyen.atomicPeriodicTable.util.ElementTranslator.getLocalizedName(this, elementName)
        val shortSymbol = allElements.find { it.element == elementName }?.short ?: "?"
        tv.text = "$shortSymbol\n$display"
    }

    // ================================================================
    // Main comparison logic: reads both JSONs and populates the table
    // ================================================================
    @SuppressLint("SetTextI18n")
    private fun updateComparisonTable() {
        val json1 = loadElementJson(element1Name) ?: return
        val json2 = loadElementJson(element2Name) ?: return
        // Element C is optional — a load failure here degrades to "---" rather than aborting
        // the whole table (json1/json2 are required, json3 is not).
        val json3 = element3Name?.let { loadElementJson(it) }

        Log.i("CompareAct", "Comparing $element1Name vs $element2Name" + (element3Name?.let { " vs $it" } ?: ""))

        // Convenience lambda for binding a row
        fun bind(label: String, key1: String, key2: String, isCategory: Boolean = false) {
            var v1 = json1.optString(key1, "---")
            var v2 = json2.optString(key2, "---")
            var v3 = if (element3Name != null) (json3?.optString(key1, "---") ?: "---") else null
            if (isCategory) {
                v1 = com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator.translate(this, v1)
                v2 = com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator.translate(this, v2)
                v3 = v3?.let { com.mckimquyen.atomicPeriodicTable.util.CategoryTranslator.translate(this, it) }
            }
            bindCompareRow(label, v1, v2, v3)
        }

        // Clear previous rows (keep header rows which are static)
        binding.compareTableBody.removeAllViews()

        // Populate rows — using all available JSON keys
        bind(getString(R.string.compare_atomic_number), "element_atomic_number", "element_atomic_number")
        bind(getString(R.string.compare_atomic_mass), "element_atomicmass", "element_atomicmass")
        bind(getString(R.string.compare_group), "element_group", "element_group", isCategory = true)
        bind(getString(R.string.compare_block), "element_block", "element_block")
        bind(getString(R.string.compare_phase), "element_phase", "element_phase")
        bind(getString(R.string.compare_electronegativity), "element_electronegativty", "element_electronegativty")
        bind(getString(R.string.compare_boiling_pt), "element_boiling_kelvin", "element_boiling_kelvin")
        bind(getString(R.string.compare_melting_pt), "element_melting_kelvin", "element_melting_kelvin")
        bind(getString(R.string.compare_density), "element_density", "element_density")
        bind(getString(R.string.compare_ionization_e), "element_ionization_energy1", "element_ionization_energy1")
        bind(getString(R.string.compare_atomic_radius), "element_atomic_radius_e", "element_atomic_radius_e")
        bind(getString(R.string.compare_covalent_radius), "element_covalent_radius", "element_covalent_radius")
        bind(getString(R.string.compare_electron_config), "element_electron_config", "element_electron_config")
        bind(getString(R.string.compare_shells), "element_shells_electrons", "element_shells_electrons")
        bind(getString(R.string.compare_year_discovered), "element_year", "element_year")
        bind(getString(R.string.compare_radioactive), "radioactive", "radioactive")
        bind(getString(R.string.compare_magnetic_type), "element_magnetic_type", "element_magnetic_type")
        bind(getString(R.string.compare_electrical_type), "electrical_type", "electrical_type")
        bind(getString(R.string.compare_fusion_heat), "element_fusion_heat", "element_fusion_heat")
        bind(getString(R.string.compare_specific_heat), "element_specific_heat_capacity", "element_specific_heat_capacity")
        bind(getString(R.string.compare_vaporization_heat), "element_vaporization_heat", "element_vaporization_heat")
    }

    // ================================================================
    // Inflate a single comparison row with color-coded indicators
    // ================================================================
    @SuppressLint("SetTextI18n")
    private fun bindCompareRow(label: String, val1: String, val2: String, val3: String? = null) {
        // Inflate row from layout
        val row = layoutInflater.inflate(R.layout.view_compare_row, binding.compareTableBody, false)
        val tvLabel = row.findViewById<TextView>(R.id.tvCompareLabel)
        val tvVal1 = row.findViewById<TextView>(R.id.tvCompareVal1)
        val tvVal2 = row.findViewById<TextView>(R.id.tvCompareVal2)
        val tvIndicator = row.findViewById<TextView>(R.id.tvCompareIndicator)
        val tvVal3 = row.findViewById<TextView>(R.id.tvCompareVal3)

        tvLabel.text = label
        tvVal1.text = val1
        tvVal2.text = val2

        val colorHighlight = ContextCompat.getColor(this, R.color.compare_higher)
        val colorLower = ContextCompat.getColor(this, R.color.compare_lower)
        val colorNeutral = com.google.android.material.color.MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSurface,
            Color.BLACK
        )

        if (val3 == null) {
            // Original 2-element behavior, untouched (regression-safe): a single ▲/▼/= indicator
            // between the two columns makes sense only for a pairwise comparison.
            val n1 = val1.toDoubleOrNull()
            val n2 = val2.toDoubleOrNull()

            when {
                n1 != null && n2 != null -> {
                    when {
                        n1 > n2 -> {
                            tvVal1.setTextColor(colorHighlight)
                            tvVal2.setTextColor(colorLower)
                            tvIndicator.text = "▲"
                            tvIndicator.setTextColor(colorHighlight)
                        }
                        n1 < n2 -> {
                            tvVal1.setTextColor(colorLower)
                            tvVal2.setTextColor(colorHighlight)
                            tvIndicator.text = "▼"
                            tvIndicator.setTextColor(colorLower)
                        }
                        else -> {
                            tvVal1.setTextColor(colorNeutral)
                            tvVal2.setTextColor(colorNeutral)
                            tvIndicator.text = "="
                            tvIndicator.setTextColor(colorNeutral)
                        }
                    }
                }
                val1 == val2 && val1 != "---" -> {
                    // Only show "=" if both values are real equal text (not both missing)
                    tvIndicator.text = "="
                    tvIndicator.setTextColor(colorNeutral)
                }
                else -> {
                    // Text differs, or one/both values are missing — no indicator
                    tvIndicator.text = ""
                }
            }
        } else {
            // 3-way compare (mục 16): a single shared ▲/▼ arrow no longer identifies which
            // column it refers to, so rank each value's OWN color instead — max → highlight,
            // min → lower, everything else (including the middle value) → neutral. Missing/
            // non-numeric values are left in the neutral color and excluded from ranking.
            tvVal3.visibility = View.VISIBLE
            tvVal3.text = val3
            tvIndicator.text = ""

            val numbers = listOf(val1, val2, val3).map { it.toDoubleOrNull() }
            val colors = com.mckimquyen.atomicPeriodicTable.feature.compare.CompareRanking.rank(numbers).map { rank ->
                when (rank) {
                    com.mckimquyen.atomicPeriodicTable.feature.compare.Rank.HIGH -> colorHighlight
                    com.mckimquyen.atomicPeriodicTable.feature.compare.Rank.LOW -> colorLower
                    com.mckimquyen.atomicPeriodicTable.feature.compare.Rank.NEUTRAL -> colorNeutral
                }
            }
            tvVal1.setTextColor(colors[0])
            tvVal2.setTextColor(colors[1])
            tvVal3.setTextColor(colors[2])
        }

        binding.compareTableBody.addView(row)
    }

    // ================================================================
    // Load JSON for a given element name from assets
    // ================================================================
    private fun loadElementJson(elementName: String): JSONObject? {
        return try {
            val inputStream: InputStream = assets.open("$elementName.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            jsonArray.getJSONObject(0)
        } catch (e: Exception) {
            // Catch Exception (not just IOException) because:
            // - JSONArray() and getJSONObject() throw JSONException (unchecked by catch IOException)
            // - If JSON is malformed or empty array → JSONException → app crash without this fix
            Log.e("CompareAct", "Failed to load JSON for $elementName: ${e.message}")
            null
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        // Set title bar height to accommodate status bar
        val params = binding.compareTitleBar.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.compareTitleBar.layoutParams = params

        // NestedScrollView is already constrained BELOW the title bar in ConstraintLayout.
        // Only add bottom padding so content is not hidden behind the navigation bar.
        binding.compareScrollView.setPadding(0, 0, 0, bottom)
    }

    override fun onDestroy() {
        // Memory leak prevention: remove TextWatcher references before super.onDestroy()
        textWatcher1?.let { binding.searchElement1.removeTextChangedListener(it) }
        textWatcher1 = null

        textWatcher2?.let { binding.searchElement2.removeTextChangedListener(it) }
        textWatcher2 = null

        textWatcher3?.let { binding.searchElement3.removeTextChangedListener(it) }
        textWatcher3 = null

        // Memory leak prevention: cancel any pending postDelayed callbacks from focus listeners
        focusHideHandler?.removeCallbacksAndMessages(null)
        focusHideHandler = null

        // Clear element list to help GC
        allElements.clear()

        super.onDestroy()
    }

    companion object {
        const val EXTRA_ELEMENT_1 = "extra_element_1"
        const val EXTRA_ELEMENT_2 = "extra_element_2"
        const val DEFAULT_THIRD_ELEMENT = "lithium"
    }
}
