package com.mckimquyen.atomicPeriodicTable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the AdMob App ID / ad-unit ID wiring introduced when switching to the
 * ca-app-pub-3004713799155145 AdMob app: catches exactly the class of mistake made while
 * doing that switch (App ID hardcoded/mismatched vs. the ad-unit IDs' own publisher account).
 * Runs per build variant (testDevDebugUnitTest / testProductionReleaseUnitTest), so it verifies
 * both the debug (Google test) and release (real) BuildConfig values independently.
 */
class AdConfigTest {

    private val appIdPattern = Regex("""^ca-app-pub-(\d+)~\d+$""")
    private val adUnitPattern = Regex("""^ca-app-pub-(\d+)/\d+$""")

    private fun publisherId(pattern: Regex, value: String): String {
        val match = pattern.matchEntire(value)
        assertTrue("'$value' does not match expected AdMob ID format", match != null)
        return match!!.groupValues[1]
    }

    @Test
    fun `IS_ENABLE_ADMOB is true`() {
        assertTrue(BuildConfig.IS_ENABLE_ADMOB)
    }

    @Test
    fun `ADMOB_APP_ID uses App ID format with tilde separator`() {
        publisherId(appIdPattern, BuildConfig.ADMOB_APP_ID)
    }

    @Test
    fun `ad unit IDs use ad-unit format with slash separator`() {
        val adUnitIds = listOf(
            BuildConfig.ADMOB_BANNER_ID,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            BuildConfig.ADMOB_APP_OPEN_ID,
            BuildConfig.ADMOB_REWARDED_ID,
        )
        adUnitIds.forEach { publisherId(adUnitPattern, it) }
    }

    @Test
    fun `ADMOB_APP_ID belongs to the same publisher account as the ad units`() {
        val appPublisher = publisherId(appIdPattern, BuildConfig.ADMOB_APP_ID)
        val bannerPublisher = publisherId(adUnitPattern, BuildConfig.ADMOB_BANNER_ID)
        val interstitialPublisher = publisherId(adUnitPattern, BuildConfig.ADMOB_INTERSTITIAL_ID)
        val appOpenPublisher = publisherId(adUnitPattern, BuildConfig.ADMOB_APP_OPEN_ID)
        val rewardedPublisher = publisherId(adUnitPattern, BuildConfig.ADMOB_REWARDED_ID)

        assertEquals("Banner ID publisher mismatch vs App ID", appPublisher, bannerPublisher)
        assertEquals("Interstitial ID publisher mismatch vs App ID", appPublisher, interstitialPublisher)
        assertEquals("App Open ID publisher mismatch vs App ID", appPublisher, appOpenPublisher)
        assertEquals("Rewarded ID publisher mismatch vs App ID", appPublisher, rewardedPublisher)
    }

    @Test
    fun `APPLOVIN_SDK_KEY is non-empty`() {
        assertTrue(BuildConfig.APPLOVIN_SDK_KEY.isNotEmpty())
    }
}
