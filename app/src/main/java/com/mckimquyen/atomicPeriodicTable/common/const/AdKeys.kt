package com.mckimquyen.atomicPeriodicTable.common.const

import com.mckimquyen.atomicPeriodicTable.BuildConfig

object AdKeys {
    const val PRIVACY_POLICY_URL: String = BuildConfig.PRIVACY_POLICY_URL

    // Injected from the private ads.properties file during the build.
    val VIP_SECRET: String get() = BuildConfig.VIP_30D_KEY
}
