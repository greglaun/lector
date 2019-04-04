package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

// This is an attempt at the MVP
interface TTSContract {
    interface AudioView { // Surely there's a better name.
        suspend fun speak(textToSpeak : String, utteranceId: String,
                          callback :suspend (String) -> Unit)
        fun stopImmediately()
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun setSpeechRate(speechRate: Float)
    }

    interface Presenter {
        fun attach(ttsView: TTSContract.AudioView?,
                   store: Store)
        fun ttsView(): AudioView?
        fun stopImmediately()

        fun onPlayButtonPressed()
        fun onPauseButtonPressed()

//        suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?)
//        suspend fun stopSpeaking()

        suspend fun onRewindOne()
        suspend fun onForwardOne()
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun setSpeechRate(speechRate: Float)
    }
}