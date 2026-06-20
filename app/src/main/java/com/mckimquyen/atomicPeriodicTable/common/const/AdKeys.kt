package com.mckimquyen.atomicPeriodicTable.common.const

import com.mckimquyen.atomicPeriodicTable.BuildConfig
import com.mckimquyen.atomicPeriodicTable.feature.vip.VipKeys

object AdKeys {
    const val PRIVACY_POLICY_URL: String = BuildConfig.PRIVACY_POLICY_URL

    // Single source of truth: VipKeys.VIP_30D_KEY
    val VIP_SECRET: String get() = VipKeys.VIP_30D_KEY
}
