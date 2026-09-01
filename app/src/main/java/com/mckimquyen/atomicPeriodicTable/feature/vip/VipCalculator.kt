package com.mckimquyen.atomicPeriodicTable.feature.vip

/**
 * Pure functions cho VIP progress & countdown — không phụ thuộc Android framework,
 * có thể test bằng JVM unit test mà không cần emulator.
 */
object VipCalculator {

    /**
     * Elapsed-semantic progress: 0% lúc vừa kích hoạt, 100% lúc hết hạn.
     *
     * Timeline: [grantedAt ───────────── expiresAt]
     *            0%                        100%
     *
     * Edge case: nếu total ≤ 0 (clock skew / đã expire) → trả 100.
     */
    fun computeElapsedProgress(grantedAtMs: Long, expiresAtMs: Long, nowMs: Long): Int {
        val total = expiresAtMs - grantedAtMs
        if (total <= 0L) return 100
        val elapsed = nowMs - grantedAtMs
        return ((elapsed.toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }

    /**
     * FIX-030: khi không biết thời điểm kích hoạt thật (storedGrantedAtMs <= 0), trả về 0
     * (unknown) thay vì tự bịa ra một mốc như "cách đây 24h" — con số bịa đó không liên quan
     * gì tới thời hạn thật (3 ngày hay 30 ngày) nên elapsed% hiển thị sẽ sai lệch hoàn toàn.
     * Caller (VipManagementAct) dùng kết quả 0L này để ẩn luôn progress bar thay vì hiện % sai.
     */
    fun resolveGrantedAtMs(storedGrantedAtMs: Long): Long = if (storedGrantedAtMs > 0L) storedGrantedAtMs else 0L

    /**
     * Tách tổng milliseconds thành [days, hours, minutes, seconds].
     * Giá trị âm được coi là 0.
     */
    fun remainingParts(totalMs: Long): LongArray {
        val ms = maxOf(0L, totalMs)
        return longArrayOf(
            ms / 86_400_000L,
            (ms / 3_600_000L) % 24L,
            (ms / 60_000L) % 60L,
            (ms / 1_000L) % 60L,
        )
    }
}
