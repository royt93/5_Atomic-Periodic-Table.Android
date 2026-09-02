package com.mckimquyen.atomicPeriodicTable.feature.vip

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mckimquyen.atomicPeriodicTable.R
import com.mckimquyen.atomicPeriodicTable.act.BaseAct
import com.mckimquyen.atomicPeriodicTable.common.const.AdKeys
import com.mckimquyen.atomicPeriodicTable.databinding.AVipManagementBinding
import com.roy.sdkadbmob.AdManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class VipManagementAct : BaseAct() {
    private lateinit var binding: AVipManagementBinding
    private lateinit var vipPrefs: VipPrefs
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var countDownTimer: CountDownTimer? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var pulseYAnimator: ObjectAnimator? = null
    private var crownAnimator: ObjectAnimator? = null
    private var entryAnimator: ValueAnimator? = null
    private var countUpAnimator: ValueAnimator? = null
    private var confettiAnimator: ObjectAnimator? = null
    private var confettiYAnimator: ObjectAnimator? = null
    private var keyTextWatcher: android.text.TextWatcher? = null
    private var lastMinute: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = AVipManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vipPrefs = VipPrefs(this)
        bindListeners()
        bindKeyWatcher()
        bindUi()  // bindUi() already calls loadRewarded when !active (see F4 fix)
        playEntryAnimation()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val toolbarParams = binding.toolbarVip.layoutParams as ViewGroup.LayoutParams
        toolbarParams.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        binding.toolbarVip.layoutParams = toolbarParams
        binding.toolbarVip.setPadding(left, top, right, 0)
        binding.scrollVip.setPadding(0, 0, 0, bottom)
        binding.scrollVip.clipToPadding = false
    }

    override fun onResume() {
        super.onResume()
        startLoopAnimations()
        bindUi()
    }

    override fun onPause() {
        stopLoopAnimations()
        super.onPause()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        countDownTimer = null
        stopLoopAnimations()
        entryAnimator?.cancel()
        entryAnimator = null
        countUpAnimator?.cancel()
        countUpAnimator = null
        confettiAnimator?.cancel()
        confettiAnimator = null
        confettiYAnimator?.cancel()
        confettiYAnimator = null
        keyTextWatcher?.let { binding.editVipKey.removeTextChangedListener(it) }
        keyTextWatcher = null
        super.onDestroy()
    }

    private fun bindKeyWatcher() {
        binding.btnRedeemVip.isEnabled = false
        keyTextWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.btnRedeemVip.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true
            }
        }
        binding.editVipKey.addTextChangedListener(keyTextWatcher)
    }

    private fun bindListeners() {
        binding.backBtnVip.setOnClickListener { finish() }
        binding.btnRedeemVip.setOnClickListener { redeemInputKey() }
        binding.btnWatchAdVip.setOnClickListener { showRewardedForVip() }
        binding.btnRevokeVip.setOnClickListener { confirmRevokeVip() }
        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AdKeys.PRIVACY_POLICY_URL)))
        }
    }

    private fun bindUi() {
        countDownTimer?.cancel()
        val active = AdManager.isVipByKeyActive()
        // F4: if VIP just expired (active=false) and rewarded not yet loaded (e.g. was skipped
        // in onCreate because VIP was active at that time), preload now so the button works.
        if (!active) AdManager.loadRewarded(this)
        val expiryMs = AdManager.getVipByKeyExpiry()
        val grantedAtMs = VipCalculator.resolveGrantedAtMs(vipPrefs.getGrantedAtMs())

        binding.heroBackground.setBackgroundResource(
            if (active) R.drawable.bg_vip_status_header_active else R.drawable.bg_vip_status_header_free
        )
        binding.tvVipStatus.text = getString(if (active) R.string.vip_active else R.string.vip_free_user)
        binding.tvVipSubtitle.text = if (active && expiryMs > 0L) {
            getString(R.string.vip_until, dateFormat.format(Date(expiryMs)))
        } else {
            getString(R.string.vip_free_user_desc)
        }

        val heroActive = active && expiryMs > 0L
        binding.rowActivated.isVisible = heroActive && grantedAtMs > 0L
        binding.rowExpires.isVisible = heroActive
        binding.tvActivatedAt.text = getString(R.string.vip_activated_at, formatDate(grantedAtMs))
        binding.tvExpiresAt.text = getString(R.string.vip_expires_at, formatDate(expiryMs))
        binding.progressVip.isVisible = heroActive && grantedAtMs > 0L
        binding.tvCountdown.isVisible = heroActive
        binding.activeVipCard.isVisible = active
        binding.btnRevokeVip.isEnabled = active
        binding.btnWatchAdVip.isEnabled = !active

        if (active && expiryMs > 0L) {
            binding.tvVipEntryLabel.text = getVipEntryLabel()
            binding.tvVipEntryExpiry.text = getString(R.string.vip_until, formatDate(expiryMs))
            startCountdown(grantedAtMs, expiryMs)
        } else {
            binding.progressVip.setProgressCompat(0, false)
            binding.tvCountdown.text = getString(R.string.vip_no_active_entry)
        }
    }

    private fun getVipEntryLabel(): String {
        if (!vipPrefs.userRedeemedAtLeastOnce()) {
            return getString(R.string.vip_entry_first_install)
        }
        val days = vipPrefs.getActivatedDays().takeIf { it > 0 } ?: 30
        return getString(R.string.vip_entry_redeemed, days)
    }

    private fun startCountdown(grantedAtMs: Long, expiresAtMs: Long) {
        val remainingMs = max(0L, expiresAtMs - System.currentTimeMillis())
        countDownTimer = object : CountDownTimer(remainingMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val now = System.currentTimeMillis()
                binding.progressVip.setProgressCompat(
                    VipCalculator.computeElapsedProgress(grantedAtMs, expiresAtMs, now),
                    true,
                )
                binding.tvCountdown.text = formatRemaining(millisUntilFinished)
                val currentMinute = (millisUntilFinished / 60_000L).toInt()
                if (lastMinute != null && lastMinute != currentMinute) {
                    countUpAnimator?.cancel()
                    countUpAnimator = ValueAnimator.ofInt(lastMinute ?: currentMinute, currentMinute).apply {
                        duration = 400L
                        start()
                    }
                }
                lastMinute = currentMinute
            }

            override fun onFinish() {
                binding.progressVip.setProgressCompat(100, true)
                bindUi()
            }
        }.start()
    }

    private fun redeemInputKey() {
        val rawKey = binding.editVipKey.text?.toString().orEmpty().trim()
        val days = VipKeys.lookupDays(rawKey)
        if (days == null) {
            showMessage(R.string.vip_failed_title, R.string.vip_invalid_key)
            return
        }
        // vipRedeemCodes (AdSdkConfig, set in RoyApp.configureAds()) is the SDK's own map of
        // key -> days and ADDS to any existing VIP time (never shortens it), so — unlike the
        // old single-secret plaintext path — the raw typed key goes straight through and no
        // "you'll lose remaining time" confirmation is needed.
        activateVip(rawKey, days)
    }

    private fun showRewardedForVip() {
        AdManager.showRewarded(this) { earned ->
            if (isFinishing || isDestroyed) return@showRewarded
            if (earned) {
                grantRewardedVip()
            } else {
                showMessage(R.string.vip_ad_unavailable_title, R.string.vip_ad_unavailable_message)
            }
        }
    }

    // SDK 1.6+: rewarded-ad grants now go through grantVipDays (no secret key needed,
    // doesn't depend on the deprecated allowLegacyPlaintextVipKey flag used by the
    // typed-key redeem flow below) — the SDK's own recommended replacement for the old
    // "hack" of calling activateVipByKey with vipKeySecret as the key.
    private fun grantRewardedVip() {
        val expiryBefore = AdManager.getVipByKeyExpiry()
        val activated = AdManager.grantVipDays(this, 3)
        val expiryAfter = AdManager.getVipByKeyExpiry()
        Log.i(
            "VipManagementAct",
            "grantRewardedVip: activated=$activated expiryBefore=$expiryBefore expiryAfter=$expiryAfter"
        )
        if (activated) {
            vipPrefs.saveGrantedAtMs(System.currentTimeMillis())
            vipPrefs.saveActivatedDays(3)
            vipPrefs.markUserRedeemed()
            performSuccessFeedback()
            bindUi()
            showMessage(R.string.vip_success_title, R.string.vip_success_message)
        } else {
            showMessage(R.string.vip_ad_unavailable_title, R.string.vip_ad_unavailable_message)
        }
    }

    private fun activateVip(key: String, days: Int) {
        // FIX-007: the actual entitlement-overwrite behavior lives inside the closed-source
        // AdManager SDK and can't be verified from this codebase alone — log the before/after
        // expiry so a real redeem-while-active bug (silently shortening remaining VIP time)
        // would be visible in production logs/crash-reporting breadcrumbs rather than invisible.
        val expiryBefore = AdManager.getVipByKeyExpiry()
        val activated = AdManager.activateVipByKey(this, key, days)
        val expiryAfter = AdManager.getVipByKeyExpiry()
        Log.i(
            "VipManagementAct",
            "activateVip: days=$days activated=$activated expiryBefore=$expiryBefore expiryAfter=$expiryAfter"
        )
        if (activated) {
            vipPrefs.saveGrantedAtMs(System.currentTimeMillis())
            vipPrefs.saveActivatedDays(days)
            vipPrefs.markUserRedeemed()
            binding.editVipKey.text = null
            performSuccessFeedback()
            bindUi()
            showMessage(R.string.vip_success_title, R.string.vip_success_message)
        } else {
            showMessage(R.string.vip_failed_title, R.string.vip_invalid_key)
        }
    }

    private fun confirmRevokeVip() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.vip_revoke_all_confirm_title)
            .setMessage(R.string.vip_revoke_all_confirm_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.confirm) { _, _ ->
                AdManager.clearVipByKey()
                vipPrefs.clearGrantedAtMs()
                vipPrefs.clearUserRedeemed()
                bindUi()
            }
            .show()
    }

    private fun showMessage(title: Int, message: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun startLoopAnimations() {
        pulseAnimator = ObjectAnimator.ofFloat(binding.btnWatchAdVip, View.SCALE_X, 1f, 1.05f).apply {
            duration = 1600L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
        pulseYAnimator = ObjectAnimator.ofFloat(binding.btnWatchAdVip, View.SCALE_Y, 1f, 1.05f).apply {
            duration = 1600L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
        crownAnimator = ObjectAnimator.ofFloat(binding.ivVipCrown, View.ROTATION, -8f, 8f).apply {
            duration = 2400L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun stopLoopAnimations() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        pulseYAnimator?.cancel()
        pulseYAnimator = null
        crownAnimator?.cancel()
        crownAnimator = null
    }

    private fun playEntryAnimation() {
        val views = listOf(binding.statusHeader, binding.btnWatchAdVip, binding.cardKeyInput)
        // Set initial state — views start invisible and below their final position
        views.forEach { v ->
            v.alpha = 0f
            v.translationY = 160f
        }
        entryAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800L
            startDelay = 80L
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                views.forEachIndexed { index, view ->
                    val local = ((value - index * 0.22f) / 0.56f).coerceIn(0f, 1f)
                    view.alpha = local
                    view.translationY = (1f - local) * 160f
                }
            }
            start()
        }
    }

    private fun performSuccessFeedback() {
        binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        confettiAnimator?.cancel()
        confettiAnimator = ObjectAnimator.ofFloat(binding.ivVipCrown, View.SCALE_X, 0.85f, 1.2f, 1f).apply {
            duration = 900L
            start()
        }
        confettiYAnimator?.cancel()
        confettiYAnimator = ObjectAnimator.ofFloat(binding.ivVipCrown, View.SCALE_Y, 0.85f, 1.2f, 1f).apply {
            duration = 900L
            start()
        }
    }

    private fun formatDate(ms: Long): String =
        if (ms > 0L) dateFormat.format(Date(ms)) else "-"

    private fun formatRemaining(ms: Long): String {
        val (days, hours, minutes, seconds) = VipCalculator.remainingParts(ms)
        return getString(R.string.vip_remaining, days, hours, minutes, seconds)
    }
}
