package com.mckimquyen.atomicPeriodicTable.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import android.view.Display
import android.view.View
import androidx.core.view.WindowInsetsCompat
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.IsotopesActExperimental
import com.mckimquyen.atomicPeriodicTable.pref.AtomicCovalentPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicRadiusCalPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicRadiusEmpPref
import com.mckimquyen.atomicPeriodicTable.pref.AtomicVanPref
import com.mckimquyen.atomicPeriodicTable.pref.BoilingPref
import com.mckimquyen.atomicPeriodicTable.pref.DegreePref
import com.mckimquyen.atomicPeriodicTable.pref.DensityPref
import com.mckimquyen.atomicPeriodicTable.pref.ElectronegativityPref
import com.mckimquyen.atomicPeriodicTable.pref.ElementSendAndLoad
import com.mckimquyen.atomicPeriodicTable.pref.FavoriteBarPref
import com.mckimquyen.atomicPeriodicTable.pref.FavoritePhase
import com.mckimquyen.atomicPeriodicTable.pref.FusionHeatPref
import com.mckimquyen.atomicPeriodicTable.pref.MeltingPref
import com.mckimquyen.atomicPeriodicTable.pref.OfflinePreference
import com.mckimquyen.atomicPeriodicTable.pref.SendIso
import com.mckimquyen.atomicPeriodicTable.pref.SpecificHeatPref
import com.mckimquyen.atomicPeriodicTable.pref.VaporizationHeatPref
import com.mckimquyen.atomicPeriodicTable.util.Pasteur
import android.util.Log
import com.mckimquyen.atomicPeriodicTable.util.ToastUtil
import com.mckimquyen.atomicPeriodicTable.util.Utils
import com.mckimquyen.atomicPeriodicTable.databinding.AElementInfoBinding
import com.squareup.picasso.Picasso
import com.squareup.picasso.OkHttp3Downloader
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.util.Locale
import kotlin.math.pow

import com.mckimquyen.atomicPeriodicTable.act.BaseAct

abstract class InfoExt : BaseAct() {
    protected lateinit var binding: AElementInfoBinding

    companion object {
        private const val TAG = "BaseActivity"
    }

    private var systemUiConfigured = false

    @SuppressLint("SetTextI18n")
    fun readJson() {
        val jsonString: String?
        binding.atomicInc.oxView.root.refreshDrawableState()

        try {
            //Setup json reader
            val elementSendAndLoadPreference = ElementSendAndLoad(this)
            val elementSendAndLoadValue = elementSendAndLoadPreference.getValue()
            if (elementSendAndLoadValue == "hydrogen") {
                binding.previousBtn.visibility = View.GONE
            } else {
                binding.previousBtn.visibility = View.VISIBLE
            }
            if (elementSendAndLoadValue == "oganesson") {
                binding.nextBtn.visibility = View.GONE
            } else {
                binding.nextBtn.visibility = View.VISIBLE
            }
            val ext = ".json"
            val elementJson = "$elementSendAndLoadValue$ext"

            //Read json
            val inputStream: InputStream = assets.open(elementJson)
            jsonString = inputStream.bufferedReader().use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)

            // Load element notes
            val notesPref = com.mckimquyen.atomicPeriodicTable.pref.NotesPref(this)
            binding.notesInput.setText(notesPref.getNote(elementSendAndLoadValue ?: ""))

            //optStrings from jsonObject or fallback
            val element = jsonObject.optString("element", "---")
            
            // Generate resource key from element name (e.g., "Hydrogen" -> "desc_hydrogen")
            // This allows us to use localized strings.xml for descriptions instead of hardcoded JSON
            val descKey = "desc_${element.lowercase(Locale.US)}"
            val descResId = resources.getIdentifier(descKey, "string", packageName)
            val description = if (descResId != 0) getString(descResId) else jsonObject.optString("description", "---")
            val url = jsonObject.optString("link", "---")
            val short = jsonObject.optString("short", "---")
            val sElementElectrons = jsonObject.optString("element_electrons", "---")
            val elementShellElectrons = jsonObject.optString("element_shells_electrons", "---")
            val sElementYear = jsonObject.optString("element_year", "---")
            val sElementDiscoveredBy = jsonObject.optString("element_discovered_name", "---")
            val sElementProtons = jsonObject.optString("element_protons", "---")
            val sElementNeutronsCommon = jsonObject.optString("element_neutron_common", "---")
            val sElementGroup = jsonObject.optString("element_group", "---")
            val elementElectronegativity = jsonObject.optString("element_electronegativty", "---")
            val wikipedia = jsonObject.optString("wikilink", "---")
            val sElementBoilingKelvin = jsonObject.optString("element_boiling_kelvin", "---")
            val sElementBoilingCelsius = jsonObject.optString("element_boiling_celsius", "---")
            val sElementBoilingFahrenheit =
                jsonObject.optString("element_boiling_fahrenheit", "---")
            val sElementMeltingKelvin = jsonObject.optString("element_melting_kelvin", "---")
            val sElementMeltingCelsius = jsonObject.optString("element_melting_celsius", "---")
            val sElementMeltingFahrenheit =
                jsonObject.optString("element_melting_fahrenheit", "---")
            val sElementAtomicNumber = jsonObject.optString("element_atomic_number", "---")
            val sElementAtomicWeight = jsonObject.optString("element_atomicmass", "---")
            val sElementDensity = jsonObject.optString("element_density", "---")
            val elementModelUrl = jsonObject.optString("element_model", "---")
            val sElementAppearance = jsonObject.optString("element_appearance", "---")
            val sElementBlock = jsonObject.optString("element_block", "---")
//            val elementCrystalStructure = jsonObject.optString("element_crystal_structure", "---")
            val fusionHeat = jsonObject.optString("element_fusion_heat", "---")
            val specificHeatCapacity = jsonObject.optString("element_specific_heat_capacity", "---")
            val vaporizationHeat = jsonObject.optString("element_vaporization_heat", "---")
            val phaseText = jsonObject.optString("element_phase", "---")

            //atomic view
            val electronConfig = jsonObject.optString("element_electron_config", "---")
            val ionCharge = jsonObject.optString("element_ion_charge", "---")
            val ionizationEnergies = jsonObject.optString("element_ionization_energy", "---")
            val atomicRadiusE = jsonObject.optString("element_atomic_radius_e", "---")
            val atomicRadius = jsonObject.optString("element_atomic_radius", "---")
            val covalentRadius = jsonObject.optString("element_covalent_radius", "---")
            val vanDerWaalsRadius = jsonObject.optString("element_van_der_waals", "---")
            val oxidationNeg1 = jsonObject.optString("oxidation_state_neg", "---")
            val oxidationPos1 = jsonObject.optString("oxidation_state_pos", "---")

            //Electromagnetic Properties
            val electricalType = jsonObject.optString("electrical_type", "---")
            val resistivity = jsonObject.optString("resistivity", "---")
            val rMultiplier = jsonObject.optString("resistivity_mult", "---")
            val magneticType = jsonObject.optString("magnetic_type", "---")
            val superconductingPoint = jsonObject.optString("superconducting_point", "---")

            //Nuclear Properties
            val isRadioactive = jsonObject.optString("radioactive", "---")
            val neutronCrossSection = jsonObject.optString("neutron_cross_sectional", "---")

            if (rMultiplier == "---") {
                binding.electromagneticInc.elementResistivity.text = "---"
            } else {
                val input = resistivity.toFloat() * rMultiplier.toFloat()
                val output = input.pow(-1).toString()
                binding.electromagneticInc.elementResistivity.text = output.replace("E", "*10^") + " (S/m)"
            }

            binding.overviewInc.descriptionName.setOnClickListener {
                binding.overviewInc.descriptionName.maxLines = 100
                binding.overviewInc.descriptionName.requestLayout()
                binding.overviewInc.dscBtn.text = "collapse"
            }
            binding.overviewInc.dscBtn.setOnClickListener {
                if (binding.overviewInc.dscBtn.text == "..more") {
                    binding.overviewInc.descriptionName.maxLines = 100
                    binding.overviewInc.descriptionName.requestLayout()
                    binding.overviewInc.dscBtn.text = "collapse"
                } else {
                    binding.overviewInc.descriptionName.maxLines = 4
                    binding.overviewInc.descriptionName.requestLayout()
                    binding.overviewInc.dscBtn.text = "..more"
                }
            }

            //set elements
            binding.elementTitle.text = element
            binding.overviewInc.descriptionName.text = description
            binding.overviewInc.elementName.text = element
            binding.overviewInc.electronsEl.text = sElementElectrons
            binding.overviewInc.elementYear.text = sElementYear
            binding.propertiesInc.elementShellsElectrons.text = elementShellElectrons
            binding.overviewInc.elementDiscoveredBy.text = sElementDiscoveredBy
            binding.overviewInc.elementElectrons.text = sElementElectrons
            binding.overviewInc.elementProtons.text = sElementProtons
            binding.overviewInc.elementNeutronsCommon.text = sElementNeutronsCommon
            binding.overviewInc.elementGroup.text = sElementGroup
            binding.temperaturesInc.elementBoilingKelvin.text = sElementBoilingKelvin
            binding.temperaturesInc.elementBoilingCelsius.text = sElementBoilingCelsius
            binding.temperaturesInc.elementBoilingFahrenheit.text = sElementBoilingFahrenheit
            binding.propertiesInc.elementElectronegativty.text = elementElectronegativity
            binding.temperaturesInc.elementMeltingKelvin.text = sElementMeltingKelvin
            binding.temperaturesInc.elementMeltingCelsius.text = sElementMeltingCelsius
            binding.temperaturesInc.elementMeltingFahrenheit.text = sElementMeltingFahrenheit
            binding.propertiesInc.elementAtomicNumber.text = sElementAtomicNumber
            binding.propertiesInc.elementAtomicWeight.text = sElementAtomicWeight
            binding.propertiesInc.elementDensity.text = sElementDensity
            binding.propertiesInc.elementBlock.text = sElementBlock
            binding.overviewInc.elementAppearance.text = sElementAppearance

            //Nuclear Properties
            binding.nuclearInc.radioactiveText.text = isRadioactive
            binding.nuclearInc.neutronCrossSectionalText.text = neutronCrossSection
            binding.nuclearInc.isotopesFrame.setOnClickListener {
                val isoPreference = ElementSendAndLoad(this)
                isoPreference.setValue(element.lowercase(Locale.getDefault())) //Send element number
                val isoSend = SendIso(this)
                isoSend.setValue("true") //Set flag for sent
                val intent = Intent(this, IsotopesActExperimental::class.java)
                startActivity(intent) //Send intent
            }

            binding.additionPhysics.tvPhaseText.text = phaseText
            binding.additionPhysics.tvFusionHeatText.text = fusionHeat
            binding.additionPhysics.tvSpecificHeatText.text = specificHeatCapacity
            binding.additionPhysics.tvVaporizationHeatText.text = vaporizationHeat

            binding.atomicInc.electronConfigText.text = electronConfig
            binding.atomicInc.ionChargeText.text = ionCharge
            binding.atomicInc.ionizationEnergiesText.text = ionizationEnergies
            binding.atomicInc.atomicRadiusText.text = atomicRadius
            binding.atomicInc.atomicRadiusEText.text = atomicRadiusE
            binding.atomicInc.covalentRadiusText.text = covalentRadius
            binding.atomicInc.vanDerWaalsRadiusText.text = vanDerWaalsRadius

            //Shell View items
            binding.shell.configData.text = elementShellElectrons
            binding.shell.eConfigData.text = electronConfig

            //Electromagnetic Properties Items
            binding.electromagneticInc.elementElectricalType.text = electricalType
            binding.electromagneticInc.elementMagneticType.text = magneticType
            binding.electromagneticInc.elementSuperconductingPoint.text = "$superconductingPoint (K)"

            // Optimized: Use when expression for mutually exclusive conditions
            when (phaseText.toString()) {
                "Solid" -> binding.additionPhysics.phaseIcon.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_vector_solid)
                )
                "Gas" -> binding.additionPhysics.phaseIcon.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_vector_gas)
                )
                "Liquid" -> binding.additionPhysics.phaseIcon.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_liquid)
                )
            }

            if (oxidationNeg1.contains(0.toString())) {
                binding.atomicInc.oxView.ox0.text = "0"
                binding.atomicInc.oxView.ox0.background.setTint(getColor(R.color.non_metals))
            }
            if (oxidationNeg1.contains(1.toString())) {
                binding.atomicInc.oxView.m1ox.text = "-1"
                binding.atomicInc.oxView.m1ox.background.setTint(getColor(R.color.noble_gas))
            }
            if (oxidationNeg1.contains(2.toString())) {
                binding.atomicInc.oxView.m2ox.text = "-2"
                binding.atomicInc.oxView.m2ox.background.setTint(getColor(R.color.noble_gas))
            }
            if (oxidationNeg1.contains(3.toString())) {
                binding.atomicInc.oxView.m3ox.text = "-3"
                binding.atomicInc.oxView.m3ox.background.setTint(getColor(R.color.noble_gas))
            }
            if (oxidationNeg1.contains(4.toString())) {
                binding.atomicInc.oxView.m4ox.text = "-4"
                binding.atomicInc.oxView.m4ox.background.setTint(getColor(R.color.noble_gas))
            }
            if (oxidationNeg1.contains(5.toString())) {
                binding.atomicInc.oxView.m5ox.text = "-5"
                binding.atomicInc.oxView.m5ox.background.setTint(getColor(R.color.noble_gas))
            }

            if (oxidationPos1.contains(1.toString())) {
                binding.atomicInc.oxView.p1ox.text = "+1"
                binding.atomicInc.oxView.p1ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(2.toString())) {
                binding.atomicInc.oxView.p2ox.text = "+2"
                binding.atomicInc.oxView.p2ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(3.toString())) {
                binding.atomicInc.oxView.p3ox.text = "+3"
                binding.atomicInc.oxView.p3ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(4.toString())) {
                binding.atomicInc.oxView.p4ox.text = "+4"
                binding.atomicInc.oxView.p4ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(5.toString())) {
                binding.atomicInc.oxView.p5ox.text = "+5"
                binding.atomicInc.oxView.p5ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(6.toString())) {
                binding.atomicInc.oxView.p6ox.text = "+6"
                binding.atomicInc.oxView.p6ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(7.toString())) {
                binding.atomicInc.oxView.p7ox.text = "+7"
                binding.atomicInc.oxView.p7ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(8.toString())) {
                binding.atomicInc.oxView.p8ox.text = "+8"
                binding.atomicInc.oxView.p8ox.background.setTint(getColor(R.color.alkali_metals))
            }
            if (oxidationPos1.contains(9.toString())) {
                binding.atomicInc.oxView.p9ox.text = "+9"
                binding.atomicInc.oxView.p9ox.background.setTint(getColor(R.color.alkali_metals))
            }

            //set element data for favorite bar
            binding.favoriteBarInclude.molarMassF.text = sElementAtomicWeight
            binding.favoriteBarInclude.phaseF.text = phaseText
            binding.favoriteBarInclude.electronegativityF.text = elementElectronegativity
            binding.favoriteBarInclude.densityF.text = sElementDensity

            val degreePref = DegreePref(this)
            val degreePrefValue = degreePref.getValue()

            if (degreePrefValue == 0) {
                binding.favoriteBarInclude.boilingF.text = sElementBoilingKelvin
                binding.favoriteBarInclude.meltingF.text = sElementMeltingKelvin
            }
            if (degreePrefValue == 1) {
                binding.favoriteBarInclude.boilingF.text = sElementBoilingCelsius
                binding.favoriteBarInclude.meltingF.text = sElementMeltingCelsius
            }
            if (degreePrefValue == 2) {
                binding.favoriteBarInclude.boilingF.text = sElementBoilingFahrenheit
                binding.favoriteBarInclude.meltingF.text = sElementMeltingFahrenheit
            }

            if (url == "empty") {
                Utils.fadeInAnim(binding.ldnPlace.noImg, 150)
                binding.ldnPlace.progressBar.visibility = View.GONE
            } else {
                Utils.fadeInAnim(binding.ldnPlace.progressBar, 150)
                binding.ldnPlace.noImg.visibility = View.GONE
            }

            binding.favoriteBarInclude.fusionHeatF.text = fusionHeat
            binding.favoriteBarInclude.specificHeatF.text = specificHeatCapacity
            binding.favoriteBarInclude.vaporizationHeatF.text = vaporizationHeat
            binding.favoriteBarInclude.aEmpiricalF.text = atomicRadiusE
            binding.favoriteBarInclude.aCalculatedF.text = atomicRadius
            binding.favoriteBarInclude.covalentF.text = covalentRadius
            binding.favoriteBarInclude.vanF.text = vanDerWaalsRadius

            val offlinePreferences = OfflinePreference(this)
            val offlinePrefValue = offlinePreferences.getValue()
            if (offlinePrefValue == 0) {
                loadImage(url)
                loadModelView(elementModelUrl)
                loadSp(short)
            }
            wikiListener(wikipedia)
        } catch (_: IOException) {
            binding.elementTitle.text = "Not able to load json"
            val stringText = "Couldn't load element:"
            val elementSendAndLoadPreference = ElementSendAndLoad(this)
            val elementSendAndLoadValue = elementSendAndLoadPreference.getValue()

            ToastUtil.showToast(this, "$stringText$elementSendAndLoadValue")
        }
    }

    private fun loadImage(url: String?) {
        try {
            Log.i("InfoExt", "roy93 loadImage url $url")
            // Optimized: Validate URL before loading to prevent errors
            if (url.isNullOrEmpty() || url == "---") {
                Log.w("InfoExt", "Invalid image URL: $url")
                binding.offlineDiv.visibility = View.VISIBLE
                binding.frame.visibility = View.GONE
                return
            }

            // Fix HTTP 403: Add custom OkHttpClient with User-Agent interceptor
            val client = OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    // Add User-Agent header to bypass Wikipedia's bot protection
                    val request = chain.request().newBuilder()
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val picasso = Picasso.Builder(this)
                .downloader(OkHttp3Downloader(client))
                .build()

            // Add Picasso callback for better debugging
            picasso
                .load(url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(binding.elementImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        Log.i("InfoExt", "roy93 loadImage SUCCESS: Image loaded for $url")
                        binding.offlineDiv.visibility = View.GONE
                        binding.frame.visibility = View.VISIBLE
                    }

                    override fun onError(e: Exception?) {
                        e?.let { Log.e("InfoExt", "roy93 loadImage ERROR: Failed to load image", it) } ?: Log.e("InfoExt", "roy93 loadImage ERROR: Failed to load image")
                        binding.offlineDiv.visibility = View.VISIBLE
                        binding.frame.visibility = View.GONE
                    }
                })
        } catch (e: Exception) {
            // Optimized: Catch all exceptions, not just ConnectException
            Log.e("InfoExt", "Failed to load image: ${e.message}", e)
            binding.offlineDiv.visibility = View.VISIBLE
            binding.frame.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSp(url: String?) {
        val hUrl = "http://www.jlindemann.se/atomic/emission_lines/"
        val ext = ".gif"
        val fURL = hUrl + url + ext
        try {
            Picasso.get().load(fURL).into(binding.propertiesInc.spImg)
            Picasso.get().load(fURL).into(binding.detailEmission.ivSpImgFetail)
        } catch (_: ConnectException) {
            binding.propertiesInc.spImg.visibility = View.GONE
            binding.propertiesInc.spOffline.text = "No Data"
            binding.propertiesInc.spOffline.visibility = View.VISIBLE
        }
    }

    private fun loadModelView(url: String?) {
        Picasso.get().load(url.toString()).into(binding.propertiesInc.modelView)
        Picasso.get().load(url.toString()).into(binding.shell.cardModelView)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun wikiListener(url: String?) {
        binding.wikipediaBtn.setOnClickListener {
            val pkgName = "com.android.chrome"
            val customTabBuilder = CustomTabsIntent.Builder()

            // Modern API: Use setDefaultColorSchemeParams instead of deprecated setToolbarColor/setSecondaryToolbarColor
            val colorParams = androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorLightPrimary))
                .build()
            customTabBuilder.setDefaultColorSchemeParams(colorParams)
            customTabBuilder.setShowTitle(true)

            val customTab = customTabBuilder.build()
            val intent = customTab.intent
            intent.data = url?.toUri()

            val packageManager = packageManager
            val resolveInfoList = packageManager.queryIntentActivities(
                customTab.intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                if (TextUtils.equals(packageName, pkgName))
                    customTab.intent.setPackage(pkgName)
            }
            customTab.intent.data?.let { it1 -> customTab.launchUrl(this, it1) }
        }
    }

    fun favoriteBarSetup() {
        //Favorite Molar
        val molarPreference = FavoriteBarPref(this)
        val molarPrefValue = molarPreference.getValue()
        if (molarPrefValue == 1) {
            binding.favoriteBarInclude.molarMassLay.visibility = View.VISIBLE
        }
        if (molarPrefValue == 0) {
            binding.favoriteBarInclude.molarMassLay.visibility = View.GONE
        }

        //Favorite Phase
        val phasePreferences = FavoritePhase(this)
        val phasePrefValue = phasePreferences.getValue()
        if (phasePrefValue == 1) {
            binding.favoriteBarInclude.phaseLay.visibility = View.VISIBLE
        }
        if (phasePrefValue == 0) {
            binding.favoriteBarInclude.phaseLay.visibility = View.GONE
        }

        //Electronegativity Phase
        val electronegativityPreferences = ElectronegativityPref(this)
        val electronegativityPrefValue = electronegativityPreferences.getValue()
        if (electronegativityPrefValue == 1) {
            binding.favoriteBarInclude.electronegativityLay.visibility = View.VISIBLE
        }
        if (electronegativityPrefValue == 0) {
            binding.favoriteBarInclude.electronegativityLay.visibility = View.GONE
        }

        //Density
        val densityPreference = DensityPref(this)
        val densityPrefValue = densityPreference.getValue()
        if (densityPrefValue == 1) {
            binding.favoriteBarInclude.densityLay.visibility = View.VISIBLE
        }
        if (densityPrefValue == 0) {
            binding.favoriteBarInclude.densityLay.visibility = View.GONE
        }

        //Boiling
        val boilingPreference = BoilingPref(this)
        val boilingPrefValue = boilingPreference.getValue()
        if (boilingPrefValue == 1) {
            binding.favoriteBarInclude.boilingLay.visibility = View.VISIBLE
        }
        if (boilingPrefValue == 0) {
            binding.favoriteBarInclude.boilingLay.visibility = View.GONE
        }

        //Melting
        val meltingPref = MeltingPref(this)
        val meltingPrefValue = meltingPref.getValue()
        if (meltingPrefValue == 1) {
            binding.favoriteBarInclude.meltingLay.visibility = View.VISIBLE
        }
        if (meltingPrefValue == 0) {
            binding.favoriteBarInclude.meltingLay.visibility = View.GONE
        }

        //Empirical
        val empiricalPreference = AtomicRadiusEmpPref(this)
        val empiricalPrefValue = empiricalPreference.getValue()
        if (empiricalPrefValue == 1) {
            binding.favoriteBarInclude.aEmpiricalLay.visibility = View.VISIBLE
        }
        if (empiricalPrefValue == 0) {
            binding.favoriteBarInclude.aEmpiricalLay.visibility = View.GONE
        }

        //Calculated
        val calculatedPreference = AtomicRadiusCalPref(this)
        val calculatedPrefValue = calculatedPreference.getValue()
        if (calculatedPrefValue == 1) {
            binding.favoriteBarInclude.aCalculatedLay.visibility = View.VISIBLE
        }
        if (calculatedPrefValue == 0) {
            binding.favoriteBarInclude.aCalculatedLay.visibility = View.GONE
        }

        //Covalent
        val covalentPreference = AtomicCovalentPref(this)
        val covalentPrefValue = covalentPreference.getValue()
        if (covalentPrefValue == 1) {
            binding.favoriteBarInclude.covalentLay.visibility = View.VISIBLE
        }
        if (covalentPrefValue == 0) {
            binding.favoriteBarInclude.covalentLay.visibility = View.GONE
        }

        //Van Der Waals
        val vanPreference = AtomicVanPref(this)
        val vanPrefValue = vanPreference.getValue()
        if (vanPrefValue == 1) {
            binding.favoriteBarInclude.vanLay.visibility = View.VISIBLE
        }
        if (vanPrefValue == 0) {
            binding.favoriteBarInclude.vanLay.visibility = View.GONE
        }

        //Fusion Heat
        val fusionHeatPref = FusionHeatPref(this)
        val fusionHeatValue = fusionHeatPref.getValue()
        if (fusionHeatValue == 1) {
            binding.favoriteBarInclude.fusionHeatLay.visibility = View.VISIBLE
        }
        if (fusionHeatValue == 0) {
            binding.favoriteBarInclude.fusionHeatLay.visibility = View.GONE
        }

        //Specific Heat
        val specificHeatPref = SpecificHeatPref(this)
        val specificHeatValue = specificHeatPref.getValue()
        if (specificHeatValue == 1) {
            binding.favoriteBarInclude.specificHeatLay.visibility = View.VISIBLE
        }
        if (specificHeatValue == 0) {
            binding.favoriteBarInclude.specificHeatLay.visibility = View.GONE
        }

        //Vaporization Heat
        val vaporizationHeatPref = VaporizationHeatPref(this)
        val vaporizationHeatValue = vaporizationHeatPref.getValue()
        if (vaporizationHeatValue == 1) {
            binding.favoriteBarInclude.vaporizationHeatLay.visibility = View.VISIBLE
        }
        if (vaporizationHeatValue == 0) {
            binding.favoriteBarInclude.vaporizationHeatLay.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableAdaptiveRefreshRate()
        }
    }

    private fun enableAdaptiveRefreshRate() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display // Sử dụng API mới
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay // Fallback cho API thấp hơn
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
}
