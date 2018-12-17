package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.POSITION_BEGINNING

// This is an attempt at the MVP
interface TTSContract {
    interface AudioView { // Surely there's a better name.
        fun speak(textToSpeak : String, utteranceId: String, callback : (String) -> Unit)
        fun stopImmediately()
        fun toggleHandsomeBritish()
    }

    interface Presenter {
        fun speakInLoop(onPositionUpdate: ((String) -> Unit)?)
        fun stopSpeaking()
        fun onUrlChanged(urlString : String, position: String = POSITION_BEGINNING)
        fun onStart(stateListener: TtsStateListener)
        fun onStop()
        fun advanceOne(onDone: (ArticleState) -> Unit)
        fun reverseOne(onDone: (ArticleState) -> Unit)
        fun toggleHandsomeBritish()
    }
}