package com.mckimquyen.atomicPeriodicTable.util

import android.os.Handler
import android.os.Looper
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout

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

    fun slideUp(panel: SlidingUpPanelLayout) {
        panel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }

    fun fadeInAnimCard(view: View, time: Long) {
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate().duration = time
        view.animate().alpha(0.85f)

    }

    fun fadeOutAnim(view: View, time: Long) {
        view.animate().duration = time
        view.animate().alpha(0.0f)
        // Use View.postDelayed instead of Handler to tie the callback to the View's lifecycle
        // This prevents memory leaks as the callback is automatically removed if the View is detached
        view.postDelayed({
            view.visibility = View.GONE
        }, time + 1)
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
