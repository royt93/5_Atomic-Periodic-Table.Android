package com.mckimquyen.atomicPeriodicTable.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class FavoriteBarPref(context: Context) {

    // Optimized: Use camelCase for private properties (Kotlin naming convention)
    private val preferenceName = "Molar_Preference"
    private val preferenceValue = "Molar_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 1)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class FavoritePhase(context: Context) {

    private val preferenceName = "Phase_Preference"
    private val preferenceValue = "Phase_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 1)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class ElectronegativityPref(context: Context) {

    private val preferenceName = "Electronegativity_Preference"
    private val preferenceValue = "Electronegativity_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 1)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class DensityPref(context: Context) {

    private val preferenceName = "Density_Preference"
    private val preferenceValue = "Density_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class BoilingPref(context: Context) {

    private val preferenceName = "Boiling_Preference"
    private val preferenceValue = "Boiling_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class MeltingPref(context: Context) {

    private val preferenceName = "Melting_Preference"
    private val preferenceValue = "Melting_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class AtomicRadiusEmpPref(context: Context) {
    private val preferenceName = "Radius_Emp_Preference"
    private val preferenceValue = "Radius_Emp_Value"
    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class AtomicRadiusCalPref(context: Context) {
    private val preferenceName = "Radius_Cal_Preference"
    private val preferenceValue = "Radius_Cal_Value"
    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class AtomicCovalentPref(context: Context) {
    private val preferenceName = "Radius_Covalent_Preference"
    private val preferenceValue = "Radius_Covalent_Value"
    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class AtomicVanPref(context: Context) {
    private val preferenceName = "Radius_Van_Preference"
    private val preferenceValue = "Radius_Van_Value"
    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class SpecificHeatPref(context: Context) {

    private val preferenceName = "Specific_Heat_Preference"
    private val preferenceValue = "Specific_Heat_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class FusionHeatPref(context: Context) {

    private val preferenceName = "Fusion_Heat_Preference"
    private val preferenceValue = "Fusion_Heat_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}

class VaporizationHeatPref(context: Context) {

    private val preferenceName = "Vaporization_Heat_Preference"
    private val preferenceValue = "Vaporization_Heat_Value"

    private val preference: SharedPreferences =
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun getValue(): Int {
        return preference.getInt(preferenceValue, 0)
    }

    fun setValue(count: Int) {
        preference.edit {
            putInt(preferenceValue, count)
        }
    }
}
