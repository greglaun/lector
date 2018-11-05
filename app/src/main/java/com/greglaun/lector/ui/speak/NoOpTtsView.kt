package com.greglaun.lector.ui.speak

class NoOpTtsView : TTSContract.AudioView {
    override fun speak(textToSpeak: String) {
        // Do nothing
    }
}