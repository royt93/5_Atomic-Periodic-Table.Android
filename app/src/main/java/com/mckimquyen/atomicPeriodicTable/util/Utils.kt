package com.mckimquyen.atomicPeriodicTable.util

import android.view.View

object Utils {
    fun fadeInAnim(
        view: View,
        time: Long,
    ) {
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate().duration = time
        view.animate().alpha(1.0f)
    }

    fun slideUpFadeIn(view: View, time: Long) {
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.translationY = 30f
        view.animate()
            .alpha(1.0f)
            .translationY(0f)
            .setDuration(time)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    fun fadeInAnimBack(view: View, time: Long) {
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate().duration = time
        view.animate().alpha(0.6f)
    }

    fun fadeOutAnim(view: View, time: Long) {
        // FIX-027: the previous postDelayed(..., time + 1) ran independently of the actual
        // animation — calling fadeOutAnim() then fadeInAnim() (or fadeOutAnim() again) before
        // that delay elapsed left the stale callback to force visibility = GONE afterwards,
        // fighting whatever the later call intended. withEndAction ties the visibility change
        // to this specific ViewPropertyAnimator run, which a later .animate() call on the
        // same view cancels/replaces before it fires.
        view.animate()
            .setDuration(time)
            .alpha(0.0f)
            .withEndAction { view.visibility = View.GONE }
            .start()
    }


//    fun jsonTransition(view: View, time: Long) {
//        //Fade out
//        view.animate().duration = time
//        view.animate().alpha(0.0f)
//        //Fade In
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            view.animate().duration = time
//            view.animate().alpha(1.0f)
//        }, time + 1)
//    }

}
