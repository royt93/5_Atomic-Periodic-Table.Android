package com.mckimquyen.atomicPeriodicTable.feature.tts

import android.speech.tts.TextToSpeech
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsAvailabilityTest {

    @Test
    fun successInit_supportedLanguage_isUsable() {
        assertTrue(TtsAvailability.isUsable(TextToSpeech.SUCCESS, TextToSpeech.LANG_AVAILABLE))
    }

    @Test
    fun errorInit_neverUsable_regardlessOfLanguageResult() {
        assertFalse(TtsAvailability.isUsable(TextToSpeech.ERROR, null))
        assertFalse(TtsAvailability.isUsable(TextToSpeech.ERROR, TextToSpeech.LANG_AVAILABLE))
    }

    @Test
    fun successInit_missingLanguageData_notUsable() {
        assertFalse(TtsAvailability.isUsable(TextToSpeech.SUCCESS, TextToSpeech.LANG_MISSING_DATA))
    }

    @Test
    fun successInit_languageNotSupported_notUsable() {
        assertFalse(TtsAvailability.isUsable(TextToSpeech.SUCCESS, TextToSpeech.LANG_NOT_SUPPORTED))
    }

    @Test
    fun successInit_nullLanguageResult_treatedAsNeverAttempted_notUsable() {
        // languageResult is only null when initStatus != SUCCESS in the real call site, but the
        // pure function must still handle it defensively rather than assume non-null.
        assertFalse(TtsAvailability.isUsable(TextToSpeech.SUCCESS, null))
    }
}
