package com.greglaun.lector.ui.speak

// This is an attempt at the MVP
interface TTSContract {
    interface AudioView { // Surely there's a better name.
        fun speak(textToSpeak : String, callback : (String) -> Unit)
        fun stopImmediately()
    }

    interface Presenter {
        fun speakInLoop()
        fun stopSpeaking()
        fun onUrlChanged(urlString : String)
        fun onStart()
        fun onStop()
        fun registerArticleOverCallback(onArticleOver: () -> Unit)
    }
}