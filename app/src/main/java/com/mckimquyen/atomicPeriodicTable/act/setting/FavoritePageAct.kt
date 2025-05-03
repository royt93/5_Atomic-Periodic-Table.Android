package com.mckimquyen.atomicPeriodicTable.act.setting

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.pref.AtomicCovalentPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicRadiusCalPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicRadiusEmpPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicVanPref
import com.mckimquyen.atomicPeriodicTable.pref.BoilingPref
import com.mckimquyen.atomicPeriodicTable.pref.DegreePref
import com.mckimquyen.atomicPeriodicTable.pref.DensityPref
import com.mckimquyen.atomicPeriodicTable.pref.ElectronegativityPref
import com.mckimquyen.atomicPeriodicTable.pref.FavoriteBarPref
import com.mckimquyen.atomicPeriodicTable.pref.FavoritePhase
import com.mckimquyen.atomicPeriodicTable.pref.FusionHeatPref
import com.mckimquyen.atomicPeriodicTable.pref.MeltingPref
import com.mckimquyen.atomicPeriodicTable.pref.SpecificHeatPref
import com.mckimquyen.atomicPeriodicTable.pref.ThemePref
import com.mckimquyen.atomicPeriodicTable.pref.VaporizationHeatPref
import kotlinx.android.synthetic.main.a_favorite_settings_page.*

class FavoritePageAct : BaseAct() {

    //TODO roy93~ admob banner
//    private var adView: MaxAdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    override fun onDestroy() {
        //TODO roy93~ admob banner
//        flAd.destroyAdBanner(adView)
        super.onDestroy()
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
        setContentView(R.layout.a_favorite_settings_page)

        val molarPreference = FavoriteBarPref(this)
        val molarPrefValue = molarPreference.getValue()
        if (molarPrefValue == 1) {
            molarMassCheck.isChecked = true
        }
        if (molarPrefValue == 0) {
            molarMassCheck.isChecked = false
        }

        val phasePreferences = FavoritePhase(this)
        val phasePrefValue = phasePreferences.getValue()
        if (phasePrefValue == 1) {
            phaseCheck.isChecked = true
        }
        if (phasePrefValue == 0) {
            phaseCheck.isChecked = false
        }

        val electronegativityPreferences = ElectronegativityPref(this)
        val electronegativityPrefValue = electronegativityPreferences.getValue()
        if (electronegativityPrefValue == 1) {
            electronegativityCheck.isChecked = true
        }
        if (electronegativityPrefValue == 0) {
            electronegativityCheck.isChecked = false
        }

        //Density
        val densityPreference = DensityPref(this)
        val densityPrefValue = densityPreference.getValue()
        if (densityPrefValue == 1) {
            densityCheck.isChecked = true
        }
        if (densityPrefValue == 0) {
            densityCheck.isChecked = false
        }

        //Degree
        val degreePref = DegreePref(this)
        val degreePrefValue = degreePref.getValue()
        if (degreePrefValue == 0) {
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        if (degreePrefValue == 1) {
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        if (degreePrefValue == 2) {
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
        }

        //Boiling Point
        val boilingPreference = BoilingPref(this)
        val boilingPrefValue = boilingPreference.getValue()
        if (boilingPrefValue == 1) {
            boilingCheck.isChecked = true
        }
        if (boilingPrefValue == 0) {
            boilingCheck.isChecked = false
        }

        //Melting Point
        val meltingPref = MeltingPref(this)
        val meltingPrefValue = meltingPref.getValue()
        if (meltingPrefValue == 1) {
            meltingCheck.isChecked = true
        }
        if (meltingPrefValue == 0) {
            meltingCheck.isChecked = false
        }

        //Atomic Radius Emp Point
        val atomicEmpPreference = AtomicRadiusEmpPref(this)
        val atomicEmpPrefValue = atomicEmpPreference.getValue()
        if (atomicEmpPrefValue == 1) {
            atomicRadiusEmpiricalCheck.isChecked = true
        }
        if (atomicEmpPrefValue == 0) {
            atomicRadiusEmpiricalCheck.isChecked = false
        }

        //Atomic Radius Cal Point
        val atomicCalPreference = AtomicRadiusCalPref(this)
        val atomicCalPrefValue = atomicCalPreference.getValue()
        if (atomicCalPrefValue == 1) {
            atomicRadiusCalculatedCheck.isChecked = true
        }
        if (atomicCalPrefValue == 0) {
            atomicRadiusCalculatedCheck.isChecked = false
        }

        //Covalent Radius Point
        val covalentPreference = AtomicCovalentPref(this)
        val atomicCovalentPrefValue = covalentPreference.getValue()
        if (atomicCovalentPrefValue == 1) {
            covalentRadiusCheck.isChecked = true
        }
        if (atomicCovalentPrefValue == 0) {
            covalentRadiusCheck.isChecked = false
        }

        //Covalent Radius Point
        val vanPreference = AtomicVanPref(this)
        val vanprefValue = vanPreference.getValue()
        if (vanprefValue == 1) {
            vanDerWaalsRadiusCheck.isChecked = true
        }
        if (vanprefValue == 0) {
            vanDerWaalsRadiusCheck.isChecked = false
        }

        //Specific Heat Capacity
        val specificHeatPref = SpecificHeatPref(this)
        val specificHeatValue = specificHeatPref.getValue()
        if (specificHeatValue == 1) {
            specificHeatCheck.isChecked = true
        }
        if (specificHeatValue == 0) {
            specificHeatCheck.isChecked = false
        }

        //Fusion Heat
        val fusionheatPref = FusionHeatPref(this)
        val fusionHeatValue = fusionheatPref.getValue()
        if (fusionHeatValue == 1) {
            fusionHeatCheck.isChecked = true
        }
        if (fusionHeatValue == 0) {
            fusionHeatCheck.isChecked = false
        }

        //Vaporization heat
        val vaporizationHeatPref = VaporizationHeatPref(this)
        val vaporizationHeatValue = vaporizationHeatPref.getValue()
        if (vaporizationHeatValue == 1) {
            vaporizationHeatCheck.isChecked = true
        }
        if (vaporizationHeatValue == 0) {
            vaporizationHeatCheck.isChecked = false
        }
        onCheckboxClicked()
        viewf.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        //Title Controller
        commonTitleBackFavColor.visibility = View.INVISIBLE
        favoriteSetTitle.visibility = View.INVISIBLE
        commonTitleBackFav.elevation = (resources.getDimension(R.dimen.zero_elevation))
        favSetScroll.viewTreeObserver
            .addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
                var y = 300f
                override fun onScrollChanged() {
                    if (favSetScroll.scrollY > 150) {
                        commonTitleBackFavColor.visibility = View.VISIBLE
                        favoriteSetTitle.visibility = View.VISIBLE
                        favoriteSetTitleDownstate.visibility = View.INVISIBLE
                        commonTitleBackFav.elevation =
                            (resources.getDimension(R.dimen.one_elevation))
                    } else {
                        commonTitleBackFavColor.visibility = View.INVISIBLE
                        favoriteSetTitle.visibility = View.INVISIBLE
                        favoriteSetTitleDownstate.visibility = View.VISIBLE
                        commonTitleBackFav.elevation =
                            (resources.getDimension(R.dimen.zero_elevation))
                    }
                    y = favSetScroll.scrollY.toFloat()
                }
            })

        backBtnFav.setOnClickListener {
            this.onBackPressed()
        }

        //TODO roy93~ admob banner
//        adView = this.createAdBanner(
//            logTag = FavoritePageAct::class.simpleName,
//            viewGroup = flAd,
//            isAdaptiveBanner = true,
//        )
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = commonTitleBackFav.layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        commonTitleBackFav.layoutParams = params

        val params2 = favoriteSetTitleDownstate.layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin =
            top + resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(
                R.dimen.header_down_margin
            )
        favoriteSetTitleDownstate.layoutParams = params2
    }

    private fun onCheckboxClicked() {
        //Molar Mass
        val molarPreference = FavoriteBarPref(this)
//        var molarPrefValue = molarPreference.getValue()
        val checkBox: CheckBox = molarMassCheck
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                molarPreference.setValue(1)
            } else {
                molarPreference.setValue(0)
            }
        }

        kelvinBtn.setOnClickListener {
            val degreePref = DegreePref(this)
//            var degreePrefValue = degreePref.getValue()

            degreePref.setValue(0)
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        celsiusBtn.setOnClickListener {
            val degreePref = DegreePref(this)
//            var degreePrefValue = degreePref.getValue()

            degreePref.setValue(1)
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
        }
        fahrenheitbtn.setOnClickListener {
            val degreePref = DegreePref(this)
//            var degreePrefValue = degreePref.getValue()

            degreePref.setValue(2)
            kelvinBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            celsiusBtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_outline)
            fahrenheitbtn.background = ContextCompat.getDrawable(this, R.drawable.shape_chip_active)
        }

        //STP Phase
        val phasePreference = FavoritePhase(this)
//        var phasePrefValue = phasePreference.getValue()
        val phaseCheckBox: CheckBox = phaseCheck
        phaseCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                phasePreference.setValue(1)
            } else {
                phasePreference.setValue(0)
            }
        }

        //Electronegativity
        val electronegativityPref = ElectronegativityPref(this)
//        var electronegativityPrefValue = electronegativityPref.getValue()
        val electronegativityCheckBox: CheckBox = electronegativityCheck
        electronegativityCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                electronegativityPref.setValue(1)
            } else {
                electronegativityPref.setValue(0)
            }
        }

        //Density
        val densityPreference = DensityPref(this)
//        var densityPrefValue = densityPreference.getValue()
        val densityCheckBox: CheckBox = densityCheck
        densityCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                densityPreference.setValue(1)
            } else {
                densityPreference.setValue(0)
            }

        }

        //Boiling Point
        val boilingPreference = BoilingPref(this)
//        var boilingPrefValue = boilingPreference.getValue()
        val boilingCheckBox: CheckBox = boilingCheck
        boilingCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                boilingPreference.setValue(1)
            } else {
                boilingPreference.setValue(0)
            }

        }

        //Melting Point
        val meltingPref = MeltingPref(this)
        val meltingCheckBox: CheckBox = meltingCheck
        meltingCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                meltingPref.setValue(1)
            } else {
                meltingPref.setValue(0)
            }
        }

        //Atomic Radius Empirical Point
        val atomicEmpiricalPreference = AtomicRadiusEmpPref(this)
        val atomicEmpiricalBox: CheckBox = atomicRadiusEmpiricalCheck
        atomicEmpiricalBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                atomicEmpiricalPreference.setValue(1)
            } else {
                atomicEmpiricalPreference.setValue(0)
            }
        }

        //Atomic Radius Calculated Point
        val atomicCalculatedPreference = AtomicRadiusCalPref(this)
        val atomicCalculatedBox: CheckBox = atomicRadiusCalculatedCheck
        atomicCalculatedBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                atomicCalculatedPreference.setValue(1)
            } else {
                atomicCalculatedPreference.setValue(0)
            }
        }

        //Covalent Radius Point
        val covalentPreference = AtomicCovalentPref(this)
        val covalentCheckBox: CheckBox = covalentRadiusCheck
        covalentCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                covalentPreference.setValue(1)
            } else {
                covalentPreference.setValue(0)
            }
        }

        //Van Der Waals Radius Point
        val vanPreference = AtomicVanPref(this)
        val vanCheckBox: CheckBox = vanDerWaalsRadiusCheck
        vanCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vanPreference.setValue(1)
            } else {
                vanPreference.setValue(0)
            }
        }

        //Specific Heat Point
        val specificHeatPref = SpecificHeatPref(this)
//        var specificHeatValue = specificHeatPref.getValue()
        val specificCheckBox: CheckBox = specificHeatCheck
        specificCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                specificHeatPref.setValue(1)
            } else {
                specificHeatPref.setValue(0)
            }

        }

        //Fusion Heat
        val fusionHeatPref = FusionHeatPref(this)
//        var fusionHeatValue = fusionHeatPref.getValue()
        val fusionCheckBox: CheckBox = fusionHeatCheck
        fusionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                fusionHeatPref.setValue(1)
            } else {
                fusionHeatPref.setValue(0)
            }

        }

        //Vapor Heat
        val vaporizationHeatPref = VaporizationHeatPref(this)
//        var vaporizationHeatValue = vaporizationHeatPref.getValue()
        val vaporizationCheckBox: CheckBox = vaporizationHeatCheck
        vaporizationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vaporizationHeatPref.setValue(1)
            } else {
                vaporizationHeatPref.setValue(0)
            }

        }

    }

}
