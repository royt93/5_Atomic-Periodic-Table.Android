package com.mckimquyen.atomicPeriodicTable.act

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Display
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.RoyApp
import com.mckimquyen.atomicPeriodicTable.act.table.DictionaryAct
import com.mckimquyen.atomicPeriodicTable.adt.ElementAdt
import com.mckimquyen.atomicPeriodicTable.anim.Anim
import com.mckimquyen.atomicPeriodicTable.ext.TableExt
import com.mckimquyen.atomicPeriodicTable.ext.moreApp
import com.mckimquyen.atomicPeriodicTable.ext.openBrowserPolicy
import com.mckimquyen.atomicPeriodicTable.ext.openUrlInBrowser
import com.mckimquyen.atomicPeriodicTable.ext.rateApp
import com.mckimquyen.atomicPeriodicTable.ext.rateAppInApp
import com.mckimquyen.atomicPeriodicTable.ext.shareApp
import com.mckimquyen.atomicPeriodicTable.feature.vip.VipManagementAct
import com.mckimquyen.atomicPeriodicTable.model.Element
import com.mckimquyen.atomicPeriodicTable.model.ElementModel
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.SearchPref
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.databinding.AMainBinding
import com.roy.sdkadbmob.AdManager
import com.mckimquyen.atomicPeriodicTable.util.Utils
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import org.deejdev.twowaynestedscrollview.TwoWayNestedScrollView
import java.util.Locale
import androidx.core.view.isVisible

class MainAct : TableExt(), ElementAdt.OnElementClickListener2 {

    private var adInitCallback: ((Boolean, String?) -> Unit)? = null
    // binding inherited from TableExt

    // FIX-008: single adapter instance, created once in setupViews() and reused by
    // filter()/searchFilter() — see IonAct.kt for the full explanation of the bug this
    // replaces.
    private lateinit var mAdapter: ElementAdt

    private var mScale = 1f
    private lateinit var mScaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    // Memory leak prevention: Store listeners for cleanup
    private var scrollChangedListener: OnScrollChangedListener? = null
    private var panelSlideListener: SlidingUpPanelLayout.PanelSlideListener? = null
    private var initNameHandler: android.os.Handler? = null
    private var filterHandler: android.os.Handler? = null
    private var textWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        Utils.fadeOutAnim(binding.navBarMain, 150)
        Utils.fadeOutAnim(binding.moreBtn, 150)
    }


    private fun setupViews() {
        binding = AMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.searchMenuInclude.rvElement
        recyclerView.layoutManager = LinearLayoutManager(/* context = */ this,/* orientation = */
            RecyclerView.VERTICAL,/* reverseLayout = */
            false
        )
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        mAdapter = ElementAdt(elementList = elements, clickListener = this, con = this)
        recyclerView.adapter = mAdapter
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), elements, recyclerView)
            }
        }
        binding.searchMenuInclude.editElement.addTextChangedListener(textWatcher)

        setOnCLickListenerSetups(elements)
        scrollAdapter()
        setupNavListeners()
        onClickNav()
        searchListener()
        binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
        searchFilter(elements)
        mediaListeners()
        hoverListeners(elements)
        initName(elements)
        binding.moreBtn.setOnClickListener { openHover() }
        binding.hoverBackground.setOnClickListener { closeHover() }
        binding.hoverMenuInclude.btRandomBtn.setOnClickListener { getRandomItem() }

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

        // Memory leak fix: Store handler for cleanup
        initNameHandler = android.os.Handler(Looper.getMainLooper())
        initNameHandler?.postDelayed({
            initName(elements)
        }, 250)

        gestureDetector = GestureDetector(/* context = */ this, /* listener = */ GestureListener())
        mScaleDetector = ScaleGestureDetector(/* context = */ this,/* listener = */
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scale = 1 - detector.scaleFactor
                    val pScale = mScale
                    mScale += scale
                    if (mScale < 1f) mScale = 1f
                    if (mScale > 12.5f) mScale = 12.5f
                    val scaleAnimation = ScaleAnimation(
                        1f / pScale, 1f / mScale, 1f / pScale, 1f / mScale, detector.focusX, detector.focusY
                    )
                    if (mScale > 1f) {
                        binding.topBar.visibility = View.GONE
                        binding.leftBar.visibility = View.GONE
                        binding.corner.visibility = View.GONE
                    }
                    if (mScale == 1f) {
                        binding.topBar.visibility = View.VISIBLE
                        binding.leftBar.visibility = View.VISIBLE
                        binding.corner.visibility = View.VISIBLE
                    }
                    scaleAnimation.duration = 0
                    scaleAnimation.fillAfter = true
                    // No cast needed - ViewBinding provides correct type
                    binding.scrollLin.startAnimation(scaleAnimation)
                    return true
                }
            })

        // Memory leak fix: Store listener for cleanup
        scrollChangedListener = object : OnScrollChangedListener {
            var y = 0f
            override fun onScrollChanged() {
                if (binding.scrollView.scrollY > y) {
                    Utils.fadeOutAnim(binding.navBarMain, 150)
                    Utils.fadeOutAnim(binding.moreBtn, 150)
                } else {
                    Utils.fadeInAnim(binding.navBarMain, 150)
                    Utils.fadeInAnim(binding.moreBtn, 150)
                }
                y = binding.scrollView.scrollY.toFloat()
            }
        }
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)

        // Memory leak fix: Store listener for cleanup
        panelSlideListener = object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {}
            override fun onPanelStateChanged(
                panel: View?,
                previousState: PanelState,
                newState: PanelState,
            ) {
                if (binding.navMenuInclude.slidingLayout.panelState === PanelState.COLLAPSED) {
                    binding.navMenuInclude.root.visibility = View.GONE
                    Utils.fadeOutAnim(view = binding.navBackground, time = 100)
                }
            }
        }
        binding.navMenuInclude.slidingLayout.addPanelSlideListener(panelSlideListener)

        // Ensure SDK is fully initialized before loading interstitial.
        // MainAct can be restored by Android before SplashAct completes init,
        // causing loadInterstitial to fire before ###init completed.
        // FIX-012 (review follow-up): same destroyed-Activity risk as SplashAct's
        // adInitCallback — deregister in onDestroy so a slow init doesn't fire late.
        adInitCallback = { _, _ ->
            AdManager.loadInterstitial(this)
        }
        (application as RoyApp).initializeAdsIfNeeded(adInitCallback!!)

        // ===============================================================
        // Back Button Handler (Modern API)
        // ===============================================================
        // Thay thế: onBackPressed() (deprecated)
        // Sử dụng: OnBackPressedDispatcher (modern, supports predictive back gesture)

        // Đăng ký callback để xử lý back button press với logic đóng panel trước
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Nếu nav menu đang hiển thị, đóng nó lại
                if (binding.navBackground.isVisible) {
                    binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
                    Utils.fadeOutAnim(binding.navBackground, 150)
                    return
                }
                // Nếu hover menu đang hiển thị, đóng nó lại
                if (binding.hoverBackground.isVisible) {
                    Utils.fadeOutAnim(binding.hoverBackground, 150)
                    Utils.fadeOutAnim(binding.hoverMenuInclude.root, 150)
                    return
                }
                // Nếu search menu đang hiển thị, đóng nó lại
                if (binding.searchMenuInclude.root.isVisible) {
                    Utils.fadeInAnim(binding.navBarMain, 150)
                    Utils.fadeOutAnim(binding.navBackground, 150)
                    Utils.fadeOutAnim(binding.searchMenuInclude.root, 150)
                    Utils.fadeInAnim(binding.moreBtn, 300)
                    return
                }
                // Nếu search filter đang hiển thị, đóng nó lại
                if (binding.searchMenuInclude.root.isVisible && binding.searchMenuInclude.background.isVisible) {
                    Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
                    Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
                    Utils.fadeInAnim(binding.moreBtn, 300)
                    return
                }
                // Không có panel nào mở, kết thúc activity
                finish()
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        mScaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event)
    }

    private class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }

    private fun scrollAdapter() {
        binding.scrollView.setOnScrollChangeListener {
                _: TwoWayNestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                _: Int,
                _: Int,
            ->
            binding.leftBar.scrollTo(/* x = */ 0, /* y = */ scrollY)
            binding.topBar.scrollTo(/* x = */ scrollX, /* y = */ 0)
        }
    }

    // FIX-014: Random Element and Search result navigation used to call startActivity()
    // directly, bypassing the interstitial gate that the grid click already applies —
    // inconsistent monetization gating between equivalent navigation paths.
    private fun navigateToElementInfoGated(intent: Intent) {
        if (AdManager.isVipByKeyActive()) {
            startActivity(intent)
        } else {
            AdManager.showInterstitial(this) { startActivity(intent) }
        }
    }

    private fun getRandomItem() {
        val elements = ArrayList<Element>()
        ElementModel.getList(elements)
        val randomNumber = (0..117).random()
        val item = elements[randomNumber]

        val elementSendAndLoad = ElementSendAndLoad(this)
        elementSendAndLoad.setValue(item.element)
        val intent = Intent(/* packageContext = */ this, /* cls = */ ElementInfoAct::class.java)
        navigateToElementInfoGated(intent)
    }

    private fun openHover() {
        Utils.fadeInAnimBack(binding.hoverBackground, 200)
        Utils.fadeInAnim(binding.hoverMenuInclude.root, 300)
    }

    private fun closeHover() {
        Utils.fadeOutAnim(binding.hoverBackground, 200)
        Utils.fadeOutAnim(binding.hoverMenuInclude.root, 300)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String, list: ArrayList<Element>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Element> = ArrayList()
        for (item in list) {
            if (item.element.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
                Log.v("MainAct", filteredList.toString())
            }
        }
        val searchPreference = SearchPref(this)
        val searchPrefValue = searchPreference.getValue()
        if (searchPrefValue == 2) {
            filteredList.sortWith { lhs, rhs ->
                // Fixed: Thay điều kiện thứ 2 từ '<' thành '>' để sort đúng
                if (lhs.element < rhs.element) -1 else if (lhs.element > rhs.element) 1 else 0
            }
        }
        mAdapter.filterList(filteredList)
        // Reuse filterHandler — cancel any pending callback before posting new one
        if (filterHandler == null) {
            filterHandler = android.os.Handler(Looper.getMainLooper())
        }
        filterHandler?.removeCallbacksAndMessages(null)
        filterHandler?.postDelayed({
            if (recyclerView.adapter?.itemCount == 0) {
                Anim.fadeIn(binding.searchMenuInclude.emptySearchBox, 300)
            } else {
                binding.searchMenuInclude.emptySearchBox.visibility = View.GONE
            }
        }, 10)
    }

    override fun elementClickListener2(item: Element, position: Int) {
        val elementSendAndLoad = ElementSendAndLoad(this)
        elementSendAndLoad.setValue(item.element)

        val intent = Intent(this, ElementInfoAct::class.java)
        navigateToElementInfoGated(intent)
    }


    private fun searchListener() {
        binding.searchBox.setOnClickListener {
            Utils.fadeInAnim(binding.searchMenuInclude.root, 300)
            binding.navBarMain.visibility = View.GONE
            Utils.fadeOutAnim(binding.moreBtn, 300)

            binding.searchMenuInclude.editElement.requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.ime())
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchMenuInclude.editElement, InputMethodManager.SHOW_IMPLICIT)
            }
            Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
        }
        binding.searchMenuInclude.closeElementSearch.setOnClickListener {
            Utils.fadeOutAnim(binding.searchMenuInclude.root, 300)
            Utils.fadeInAnim(binding.moreBtn, 300)
            binding.navBarMain.visibility = View.VISIBLE

            val view = this.currentFocus
            if (view != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    binding.viewMain.doOnLayout {
                        window.insetsController?.hide(WindowInsets.Type.ime())
                    }
                } else {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }
    }

    private fun mediaListeners() {
        binding.navMenuInclude.btRate.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            rateApp(packageName)
        }
        binding.navMenuInclude.btMore.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            moreApp()
        }
        binding.navMenuInclude.btShare.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            shareApp()
        }
        binding.navMenuInclude.btPolicy.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            openBrowserPolicy()
        }
        binding.navMenuInclude.btGithub.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            openUrlInBrowser("https://github.com/JLindemann42/Atomic-Periodic-Table.Android")
        }
    }

    private fun onClickNav() {
        binding.menuBtn.setOnClickListener {
            binding.navMenuInclude.root.visibility = View.VISIBLE
            binding.navBackground.visibility = View.VISIBLE
            Utils.fadeInAnimBack(binding.navBackground, 200)
            binding.navMenuInclude.slidingLayout.panelState = PanelState.EXPANDED
        }
        binding.navBackground.setOnClickListener {
            binding.searchMenuInclude.root.visibility = View.GONE
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            binding.navBarMain.visibility = View.VISIBLE
            Utils.fadeOutAnim(binding.navBackground, 100)
        }
        binding.navMenuInclude.solubilityBtn.setOnClickListener {
            val intent = Intent(this, TableAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.isotopesBtn.setOnClickListener {
            val intent = Intent(this, IsotopesActExperimental::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.dictionaryBtn.setOnClickListener {
            val intent = Intent(this, DictionaryAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.compareBtn.setOnClickListener {
            binding.navMenuInclude.slidingLayout.panelState = PanelState.COLLAPSED
            val intent = Intent(this, CompareAct::class.java).apply {
                putExtra(CompareAct.EXTRA_ELEMENT_1, "hydrogen")
                putExtra(CompareAct.EXTRA_ELEMENT_2, "helium")
            }
            startActivity(intent)
        }
        binding.navMenuInclude.menuMolarMassBtn.setOnClickListener {
            val intent = Intent(this, CalculatorAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.menuBalancerBtn.setOnClickListener {
            val intent = Intent(this, EquationBalancerAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.menuQuizBtn.setOnClickListener {
            val intent = Intent(this, QuizAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.menuFlashcardBtn.setOnClickListener {
            val intent = Intent(this, FlashcardAct::class.java)
            startActivity(intent)
        }
        binding.navMenuInclude.menuTrendsBtn.setOnClickListener {
            val intent = Intent(this, TrendsChartAct::class.java)
            startActivity(intent)
        }
    }

    private fun hoverListeners(elements: ArrayList<Element>) {
        binding.hoverMenuInclude.hNameBtn.setOnClickListener {
            initName(elements)
        }
        binding.hoverMenuInclude.hGroupBtn.setOnClickListener {
            initGroups(elements)
        }
        binding.hoverMenuInclude.hElectronegativityBtn.setOnClickListener {
            initElectro(elements)
        }
        binding.hoverMenuInclude.atomicWeightBtn.setOnClickListener {
            initWeight(elements)
        }
        binding.hoverMenuInclude.boilingBtn.setOnClickListener {
            initBoiling(elements)
        }
        binding.hoverMenuInclude.meltingPoint.setOnClickListener {
            initMelting(elements)
        }
        binding.hoverMenuInclude.hPhaseBtn.setOnClickListener {
            initPhase(elements)
        }
        binding.hoverMenuInclude.hYearBtn.setOnClickListener {
            initYear(elements)
        }
        binding.hoverMenuInclude.hFusionBtn.setOnClickListener {
            initHeat(elements)
        }
        binding.hoverMenuInclude.hSpecificBtn.setOnClickListener {
            initSpecific(elements)
        }
        binding.hoverMenuInclude.hVaporizatonBtn.setOnClickListener {
            initVape(elements)
        }
    }

    private fun setupNavListeners() {
        binding.settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsAct::class.java)
            startActivity(intent)
        }
        binding.btnVipMenu.setOnClickListener {
            val intent = Intent(this, VipManagementAct::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun setOnCLickListenerSetups(list: ArrayList<Element>) {
        for (item in list) {
            val name = item.element
            val extBtn = "_btn"
            val eViewBtn = "$name$extBtn"
            val resIDB = resources.getIdentifier(eViewBtn, "id", packageName)

            val btn = findViewById<TextView>(resIDB)
            btn.foreground = ContextCompat.getDrawable(/* context = */ this,/* id = */ R.drawable.ripple_t_ripple
            )
            btn.isClickable = true
            btn.isFocusable = true
            btn.setOnClickListener {
                val elementSend = ElementSendAndLoad(this)
                elementSend.setValue(item.element)
                val intent = Intent(this, ElementInfoAct::class.java)
                navigateToElementInfoGated(intent)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchFilter(list: ArrayList<Element>) {
        binding.searchMenuInclude.filterBox.root.visibility = View.GONE
        binding.searchMenuInclude.background.visibility = View.GONE

        binding.searchMenuInclude.btFilterBtn.setOnClickListener {
            Utils.fadeInAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeInAnim(binding.searchMenuInclude.background, 150)
        }
        binding.searchMenuInclude.background.setOnClickListener {
            Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
        }
        binding.searchMenuInclude.filterBox.elmtNumbBtn2.setOnClickListener {
            binding.searchMenuInclude.editElement.setText("")

            val searchPreference = SearchPref(this)
            searchPreference.setValue(0)

            val filtList: ArrayList<Element> = ArrayList()
            for (item in list) {
                filtList.add(item)
            }
            Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
            mAdapter.filterList(filtList)
        }
        binding.searchMenuInclude.filterBox.electroBtn.setOnClickListener {
            binding.searchMenuInclude.editElement.setText("")

            val searchPreference = SearchPref(this)
            searchPreference.setValue(1)

            val filtList: ArrayList<Element> = ArrayList()
            for (item in list) {
                filtList.add(item)
            }
            Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
            mAdapter.filterList(filtList)
        }
        binding.searchMenuInclude.filterBox.alphabetBtn.setOnClickListener {
            binding.searchMenuInclude.editElement.setText("")

            val searchPreference = SearchPref(this)
            searchPreference.setValue(2)

            val filtList: ArrayList<Element> = ArrayList()
            for (item in list) {
                filtList.add(item)
            }
            Utils.fadeOutAnim(binding.searchMenuInclude.filterBox.root, 150)
            Utils.fadeOutAnim(binding.searchMenuInclude.background, 150)
            filtList.sortWith { lhs, rhs ->
                // Fixed: Thay điều kiện thứ 2 từ '<' thành '>' để sort đúng
                if (lhs.element < rhs.element) -1 else if (lhs.element > rhs.element) 1 else 0
            }
            mAdapter.filterList(filtList)
        }
    }

    override fun onApplySystemInsets(
        top: Int,
        bottom: Int,
        left: Int,
        right: Int,
    ) {
        // Nav panel: do NOT override height (it is wrap_content in XML so it sizes to content).
        // Only apply bottom padding on navLin so content clears the system navigation bar.
        binding.navMenuInclude.navLin.setPadding(
            left,
            0,
            right,
            bottom
        )

        val params = binding.commonTitleBackMain.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar_main)
        binding.commonTitleBackMain.layoutParams = params

        val params2 = binding.navBarMain.layoutParams as ViewGroup.LayoutParams
        params2.height = bottom + resources.getDimensionPixelSize(R.dimen.nav_bar)
        binding.navBarMain.layoutParams = params2

        val params3 = binding.moreBtn.layoutParams as ViewGroup.MarginLayoutParams
        params3.bottomMargin =
            bottom + (resources.getDimensionPixelSize(R.dimen.nav_bar)) / 2 + (resources.getDimensionPixelSize(
                R.dimen.title_bar_elevation
            ))
        binding.moreBtn.layoutParams = params3

        val params4 = binding.searchMenuInclude.commonTitleBackSearch.layoutParams as ViewGroup.LayoutParams
        params4.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.searchMenuInclude.commonTitleBackSearch.layoutParams = params4

        binding.searchMenuInclude.rvElement.setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar)
        )

        val navSide = binding.navContent.layoutParams as ViewGroup.MarginLayoutParams
        navSide.rightMargin = right
        navSide.leftMargin = left
        binding.navContent.layoutParams = navSide

        // searchBox: left inset + visual margin. Right is handled by layout_weight=1 + pill's marginEnd
        val barSide = binding.searchBox.layoutParams as ViewGroup.MarginLayoutParams
        barSide.leftMargin = left + resources.getDimensionPixelSize(R.dimen.search_margin_side)
        binding.searchBox.layoutParams = barSide

        // pill: right inset + fixed 8dp visual margin
        val pillParams = binding.btnVipMenu.layoutParams as ViewGroup.MarginLayoutParams
        pillParams.rightMargin = right + resources.getDimensionPixelSize(R.dimen.margin_space_card)
        binding.btnVipMenu.layoutParams = pillParams

        val leftScrollBar = binding.leftBar.layoutParams as ViewGroup.MarginLayoutParams
        leftScrollBar.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar_main) + resources.getDimensionPixelSize(
                R.dimen.left_bar
            )
        binding.leftBar.layoutParams = leftScrollBar

        val topScrollBar = binding.topBar.layoutParams as ViewGroup.MarginLayoutParams
        topScrollBar.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar_main)
        binding.topBar.layoutParams = topScrollBar

        val numb = binding.leftBar.layoutParams as ViewGroup.LayoutParams
        numb.width = left + resources.getDimensionPixelSize(R.dimen.left_bar)
        binding.leftBar.layoutParams = numb

        val cornerP = binding.corner.layoutParams as ViewGroup.LayoutParams
        cornerP.width = left + resources.getDimensionPixelSize(R.dimen.left_bar)
        binding.corner.layoutParams = cornerP

        val cornerM = binding.corner.layoutParams as ViewGroup.MarginLayoutParams
        cornerM.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar_main)
        binding.corner.layoutParams = cornerM

        val params5 = binding.hoverMenuInclude.root.layoutParams as ViewGroup.MarginLayoutParams
        params5.bottomMargin = bottom
        binding.hoverMenuInclude.root.layoutParams = params5

        val params6 = binding.scrollView.layoutParams as ViewGroup.MarginLayoutParams
        params6.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar_main)
        binding.scrollView.layoutParams = params6

        // Note: slidingLayout height is intentionally NOT overridden here.
        // The XML uses wrap_content so the panel sizes to its content automatically.
        // Bottom inset is already applied via navLin padding above.

        val searchEmptyImgPrm = binding.searchMenuInclude.emptySearchBox.layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        binding.searchMenuInclude.emptySearchBox.layoutParams = searchEmptyImgPrm

        // Fix: Add bottom padding to scrollLin to prevent content being hidden behind bottom nav bar
        binding.scrollLin.setPadding(
            binding.scrollLin.paddingLeft,
            binding.scrollLin.paddingTop,
            binding.scrollLin.paddingRight,
            bottom + resources.getDimensionPixelSize(R.dimen.nav_bar) + resources.getDimensionPixelSize(R.dimen.title_bar_elevation)
        )
    }


//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            interstitialAd = MaxInterstitialAd(getString(R.string.INTER), this)
//            interstitialAd?.let { ad ->
//                ad.setListener(object : MaxAdListener {
//                    override fun onAdLoaded(p0: MaxAd) {
////                        logI("onAdLoaded")
////                        retryAttempt = 0
//                    }
//
//                    override fun onAdDisplayed(p0: MaxAd) {
////                        logI("onAdDisplayed")
//                    }
//
//                    override fun onAdHidden(p0: MaxAd) {
////                        logI("onAdHidden")
//                        // Interstitial Ad is hidden. Pre-load the next ad
//                        interstitialAd?.loadAd()
//                    }
//
//                    override fun onAdClicked(p0: MaxAd) {
////                        logI("onAdClicked")
//                    }
//
//                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
////                        logI("onAdLoadFailed")
////                        retryAttempt++
////                        val delayMillis =
////                            TimeUnit.SECONDS.toMillis(2.0.pow(min(6, retryAttempt)).toLong())
////
////                        Handler(Looper.getMainLooper()).postDelayed(
////                            {
////                                interstitialAd?.loadAd()
////                            }, delayMillis
////                        )
//                    }
//
//                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
////                        logI("onAdDisplayFailed")
//                        // Interstitial ad failed to display. We recommend loading the next ad.
//                        interstitialAd?.loadAd()
//                    }
//
//                })
//                ad.setRevenueListener {
////                    logI("onAdDisplayed")
//                }
//
//                // Load the first ad.
//                ad.loadAd()
//            }
//        }
//    }
//
//    private fun showAd(runnable: Runnable? = null) {
//        val enableAdInter = getString(R.string.EnableAdInter) == "true"
//        if (enableAdInter) {
//            if (interstitialAd == null) {
//                runnable?.run()
//            } else {
//                interstitialAd?.let { ad ->
//                    if (ad.isReady) {
////                        showDialogProgress()
////                        setDelay(500.getRandomNumber() + 500) {
////                            hideDialogProgress()
////                            ad.showAd()
////                            runnable?.run()
////                        }
//                        if (BuildConfig.DEBUG) {
//                            Toast.makeText(this@MainAct, "interstitialAd showAd SUCCESSFULLY", Toast.LENGTH_SHORT)
//                                .show()
//                        } else {
//                            ad.showAd()
//                        }
//                        runnable?.run()
//                    } else {
//                        runnable?.run()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "Applovin show ad Inter in debug mode", Toast.LENGTH_SHORT).show()
//            runnable?.run()
//        }
//    }

    private var lastVipActive = false

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableAdaptiveRefreshRate()
        }
        rateAppInApp(forceRateInApp = false)
        bindToolbarVipBadge()
    }

    private fun bindToolbarVipBadge() {
        val active = AdManager.isVipByKeyActive()
        val stateChanged = active != lastVipActive
        lastVipActive = active

        binding.tvVipPillLabel.text = getString(
            if (active) R.string.vip_title else R.string.vip_pill_free
        )
        binding.btnVipMenu.setBackgroundResource(
            if (active) R.drawable.bg_vip_pill_active else R.drawable.bg_vip_pill_free
        )
        val iconTint = if (active) {
            android.graphics.Color.parseColor("#B7791F")
        } else {
            androidx.core.content.ContextCompat.getColor(this, R.color.colorThemeTextSecondary)
        }
        binding.ivVipPillIcon.setColorFilter(iconTint)
        binding.tvVipPillLabel.setTextColor(
            if (active) android.graphics.Color.parseColor("#7C2D12")
            else androidx.core.content.ContextCompat.getColor(this, R.color.colorThemeTextSecondary)
        )

        if (stateChanged) {
            binding.btnVipMenu.animate()
                .scaleX(1.2f).scaleY(1.2f).setDuration(120)
                .withEndAction {
                    binding.btnVipMenu.animate()
                        .scaleX(1f).scaleY(1f).setDuration(180).start()
                }.start()
        }
    }

    private fun enableAdaptiveRefreshRate() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display // Sử dụng API mới
        } else {
            @Suppress("DEPRECATION") wm.defaultDisplay // Fallback cho API thấp hơn
        }


        if (display != null) {
            val supportedModes = display.supportedModes
            val highestRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }

            if (highestRefreshRateMode != null) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = highestRefreshRateMode.modeId
                }
                println("Adaptive refresh rate applied: ${highestRefreshRateMode.refreshRate} Hz")
            }
        }
    }

    override fun onDestroy() {
        // Memory leak fix: Clean up handlers and listeners
        initNameHandler?.removeCallbacksAndMessages(null)
        initNameHandler = null

        filterHandler?.removeCallbacksAndMessages(null)
        filterHandler = null

        scrollChangedListener?.let {
            binding.scrollView.viewTreeObserver.removeOnScrollChangedListener(it)
        }
        scrollChangedListener = null

        panelSlideListener?.let {
            binding.navMenuInclude.slidingLayout.removePanelSlideListener(it)
        }
        panelSlideListener = null

        textWatcher?.let {
            binding.searchMenuInclude.editElement.removeTextChangedListener(it)
        }
        textWatcher = null

        adInitCallback?.let { (application as RoyApp).removePendingAdInitCallback(it) }
        adInitCallback = null

        super.onDestroy()
    }
}
