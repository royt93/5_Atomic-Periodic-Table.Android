package com.mckimquyen.atomicPeriodicTable.feature.tts

import android.speech.tts.TextToSpeech

/**
 * Pure decision logic split out of ElementInfoAct's TextToSpeech.OnInitListener callback so it's
 * JVM-testable without a real TTS engine — only references TextToSpeech's int constants, no
 * instance methods (safe under unitTests.returnDefaultValues=true).
 */
object TtsAvailability {
    fun isUsable(initStatus: Int, languageResult: Int?): Boolean =
        initStatus == TextToSpeech.SUCCESS &&
            languageResult != null &&
            languageResult != TextToSpeech.LANG_MISSING_DATA &&
            languageResult != TextToSpeech.LANG_NOT_SUPPORTED
}
