package com.mckimquyen.atomicPeriodicTable.util

import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for a fontScale accessibility bug: setLocale() used to hardcode
 * config.fontScale = 1.0f on every language change, silently overriding the user's
 * system-wide "font size" accessibility setting. The applied configuration must keep
 * whatever fontScale the context already had.
 */
@RunWith(AndroidJUnit4::class)
class LocaleHelperFontScaleTest {

    @Test
    fun setLocale_preservesTheContextsExistingFontScale_insteadOfForcingItTo1() {
        val appContext = ApplicationProvider.getApplicationContext<android.content.Context>()
        val baseConfig = Configuration(appContext.resources.configuration)
        baseConfig.fontScale = 1.3f
        val scaledContext = appContext.createConfigurationContext(baseConfig)

        val result = LocaleHelper.setLocale(scaledContext, "en")

        assertEquals(
            "setLocale() must not override the caller's existing fontScale",
            1.3f,
            result.resources.configuration.fontScale,
            0.001f,
        )
    }
}
