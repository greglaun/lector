package com.greglaun.lector.ui.speak

import com.greglaun.lector.android.utteranceId

class NoOpTtsView : TTSContract.AudioView {
    override fun speak(textToSpeak: String, callback : (String) -> Unit) {
        // Do nothing
        if (callback  != null) {
            callback(utteranceId(textToSpeak))
        }
    }

    override fun stopImmediately() {
        // Do nothing
    }
}