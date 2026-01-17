package com.mckimquyen.atomicPeriodicTable.util

import com.mckimquyen.atomicPeriodicTable.sdkadbmob.Logger

/**
 * Legacy logging utility - now delegates to Logger for consistent logging.
 * Kept for backward compatibility with existing code.
 */
@Suppress("unused", "MemberVisibilityCanPrivate")
object Pasteur {
    private const val DEFAULT_TAG = "ATOMIC"

    private var debugMode: Boolean = false

    fun init(debug: Boolean) {
        debugMode = debug
    }

    fun d(tag: String?, string: String) {
        debug(tag, string)
    }

    private fun debug(tag: String?, string: String) {
        // Delegate to Logger which handles debug mode check internally
        Logger.i(tag ?: DEFAULT_TAG, string)
    }

    fun i(tag: String?, string: String) {
        info(tag, string)
    }

    fun info(tag: String?, string: String) {
        // Delegate to Logger which handles debug mode check internally
        Logger.i(tag ?: DEFAULT_TAG, string)
    }

    fun w(tag: String?, string: String) {
        warn(tag, string)
    }

    private fun warn(tag: String?, string: String) {
        // Delegate to Logger which handles debug mode check internally
        Logger.w("[$tag] $string")
    }

    fun e(tag: String?, string: String) {
        error(tag, string)
    }

    private fun error(tag: String?, string: String) {
        // Delegate to Logger which handles debug mode check internally
        Logger.e("[$tag] $string")
    }
}
