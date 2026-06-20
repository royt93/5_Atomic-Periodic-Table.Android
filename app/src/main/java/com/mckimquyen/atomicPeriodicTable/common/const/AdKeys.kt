package com.mckimquyen.atomicPeriodicTable.common.const

import com.mckimquyen.atomicPeriodicTable.BuildConfig

object AdKeys {
    const val PRIVACY_POLICY_URL: String = BuildConfig.PRIVACY_POLICY_URL

    val VIP_SECRET: String by lazy {
        String(java.util.Base64.getDecoder().decode(VIP_30D_B64))
    }

    private const val VIP_30D_B64 = "OWZBMHE3ZU4hMjdjTHgwNEAyMTk5M1kydTBJNyNRMA=="
}
