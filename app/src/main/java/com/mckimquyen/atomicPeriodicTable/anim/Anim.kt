package com.mckimquyen.atomicPeriodicTable.anim

import android.view.View

object Anim {

    fun fadeIn(view: View, time: Long) {
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate().duration = time
        view.animate().alpha(1.0f)
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

}
