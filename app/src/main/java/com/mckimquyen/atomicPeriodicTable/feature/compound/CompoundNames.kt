package com.mckimquyen.atomicPeriodicTable.feature.compound

import androidx.annotation.StringRes
import com.mckimquyen.atomicPeriodicTable.R

/** Localized display names for MolarMassQuestionGenerator.COMMON_FORMULAS. */
object CompoundNames {
    private val nameResByFormula: Map<String, Int> = mapOf(
        "H2O" to R.string.compound_name_h2o,
        "CO2" to R.string.compound_name_co2,
        "NaCl" to R.string.compound_name_nacl,
        "CH4" to R.string.compound_name_ch4,
        "NH3" to R.string.compound_name_nh3,
        "O2" to R.string.compound_name_o2,
        "H2SO4" to R.string.compound_name_h2so4,
        "HCl" to R.string.compound_name_hcl,
        "CaCO3" to R.string.compound_name_caco3,
        "C6H12O6" to R.string.compound_name_c6h12o6,
        "NaOH" to R.string.compound_name_naoh,
        "KMnO4" to R.string.compound_name_kmno4,
        "Fe2O3" to R.string.compound_name_fe2o3,
        "AgNO3" to R.string.compound_name_agno3,
        "Al2O3" to R.string.compound_name_al2o3,
    )

    @StringRes
    fun nameResFor(formula: String): Int? = nameResByFormula[formula]
}
