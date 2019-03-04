package com.greglaun.lector.ui.speak

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TtsPresenter(private val tts: TTSContract.AudioView,
                   val stateMachine: TtsStateMachine)
    : TTSContract.Presenter, TtsActorClient {
    var onPositionUpdate: ((AbstractArticleState) -> Unit)? = null

    override fun speechViewSpeak(text: String, utteranceId: String, callback: (String) -> Unit) {
        synchronized(tts) {
            tts.speak(text, utteranceId) {
                callback(it)
            }
        }
    }

    override fun onStart(stateListener: TtsStateListener) {
        stateMachine?.startMachine(this, stateListener)
    }

    override fun onStop() {
        stateMachine?.stopMachine()
    }

    override suspend fun speakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        this.onPositionUpdate = onPositionUpdate
        stateMachine?.actionSpeakInLoop(onPositionUpdate)
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override suspend fun onArticleChanged(articleState: ArticleState) {
        stateMachine?.updateArticle(articleState)
    }

    override suspend fun stopSpeaking() {
        stateMachine?.actionStopSpeaking()
    }

    override fun advanceOne(onDone: (ArticleState) -> Unit) {
        GlobalScope.launch {
            stateMachine?.stopAdvanceOneAndResume(onDone)
        }
    }

    override fun reverseOne(onDone: (ArticleState) -> Unit) {
        GlobalScope.launch {
            stateMachine?.stopReverseOneAndResume(onDone)
        }
    }

    override fun setHandsomeBritish(shouldBeBritish: Boolean) {
        tts.setHandsomeBritish(shouldBeBritish)
    }

    override fun setSpeechRate(speechRate: Float) {
        tts.setSpeechRate(speechRate)
    }
}