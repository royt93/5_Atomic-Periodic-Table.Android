package com.mckimquyen.atomicPeriodicTable.feature.vip

object VipKeys {
    val VIP_30D_KEY: String by lazy { decode(VIP_30D_B64) }
    val VIP_3D_KEY: String by lazy { decode(VIP_3D_B64) }

    private val keyToDays: Map<String, Int> by lazy {
        mapOf(
            VIP_30D_KEY to 30,
            VIP_3D_KEY to 3,
        )
    }

    fun lookupDays(rawInput: String): Int? = keyToDays[rawInput.trim()]

    private fun decode(value: String): String =
        String(android.util.Base64.decode(value, android.util.Base64.NO_WRAP))

    private const val VIP_30D_B64 = "OWZBMHE3ZU4hMjdjTHgwNEAyMTk5M1kydTBJNyNRMA=="
    private const val VIP_3D_B64 = "ZVE3QDkzTDBmITJZMjcwN3hOMDQwMjE5OTN1MEkjMmFL"
}
