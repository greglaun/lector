package com.greglaun.lector.ui.speak

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
        fun stopImmediately()
        suspend fun deprecatedSpeakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?)
        suspend fun deprecatedStopSpeaking()
        suspend fun deprecatedOnArticleChanged(articleState: ArticleState)
        fun deprecatedOnStart(stateListener: TtsStateListener)
        fun deprecatedOnStop()
        fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit)
        fun deprecatedReverseOne(onDone: (ArticleState) -> Unit)
        fun deprecatedHandsomeBritish(shouldBeBritish: Boolean)
        fun deprecatedSetSpeechRate(speechRate: Float)
    }
}