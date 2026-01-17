package com.mckimquyen.atomicPeriodicTable.sdkadbmob

import android.util.Log
import com.mckimquyen.atomicPeriodicTable.BuildConfig

/**
 * Centralized logging utility class.
 * Only logs in debug mode to prevent log leakage in production.
 */
object Logger {
    private const val TAG = "roy93~"

    /**
     * Log info message (uses Log.d internally)
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    /**
     * Log info message with custom sub-tag
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun i(subTag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[$subTag] $message")
        }
    }

    /**
     * Log error message
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun e(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message)
        }
    }

    /**
     * Log error message with throwable
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun e(message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, throwable)
        }
    }

    /**
     * Log warning message
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun w(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message)
        }
    }

    /**
     * Log verbose message
     * Only logs when BuildConfig.DEBUG is true
     */
    @JvmStatic
    fun v(message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message)
        }
    }
}
