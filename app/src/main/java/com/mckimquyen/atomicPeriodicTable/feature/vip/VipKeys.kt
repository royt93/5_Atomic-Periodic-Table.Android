package com.mckimquyen.atomicPeriodicTable.feature.vip

import com.mckimquyen.atomicPeriodicTable.BuildConfig

object VipKeys {
    val VIP_30D_KEY: String get() = BuildConfig.VIP_30D_KEY
    val VIP_3D_KEY: String get() = BuildConfig.VIP_3D_KEY

    private val keyToDays: Map<String, Int> by lazy {
        mapOf(
            VIP_30D_KEY to 30,
            VIP_3D_KEY to 3,
        )
    }

    fun lookupDays(rawInput: String): Int? = keyToDays[rawInput.trim()]
}
