package com.greglaun.lector.ui.speak

// This is an attempt at the MVP
interface TTSContract {
    interface AudioView { // Surely there's a better name.
        fun speak(textToSpeak : String, utteranceId: String, callback : (String) -> Unit)
        fun stopImmediately()
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun setSpeechRate(speechRate: Float)
    }

    interface Presenter {
        fun speakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?)
        fun stopSpeaking()
        suspend fun onArticleChanged(articleState: ArticleState)
        fun onStart(stateListener: TtsStateListener)
        fun onStop()
        fun advanceOne(onDone: (ArticleState) -> Unit)
        fun reverseOne(onDone: (ArticleState) -> Unit)
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun setSpeechRate(speechRate: Float)
    }
}