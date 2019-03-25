package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TtsPresenter(private val tts: TTSContract.AudioView,
                   val stateMachine: DeprecatedTtsStateMachine,
                   val store: Store)
    : TTSContract.Presenter, TtsActorClient {
    var onPositionUpdate: ((AbstractArticleState) -> Unit)? = null

    override suspend fun speechViewSpeak(text: String, utteranceId: String,
                                         callback: suspend (String) -> Unit) {
        tts.speak(text, utteranceId) {
            callback(it)
        }
    }

    override fun stopImmediately() {
        tts.stopImmediately()
    }


    override fun deprecatedOnStart(stateListener: TtsStateListener) {
        stateMachine?.attach(this, stateListener, store)
    }

    override fun deprecatedOnStop() {
//        stateMachine?.detach()
    }

    override suspend fun deprecatedSpeakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        this.onPositionUpdate = onPositionUpdate
        stateMachine?.actionSpeakInLoop(onPositionUpdate)
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override suspend fun deprecatedOnArticleChanged(articleState: ArticleState) {
        stateMachine?.updateArticle(articleState)
    }

    override suspend fun deprecatedStopSpeaking() {
        stateMachine?.actionStopSpeaking()
    }

    override fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit) {
        GlobalScope.launch {
            stateMachine?.stopAdvanceOneAndResume(onDone)
        }
    }

    override fun deprecatedReverseOne(onDone: (ArticleState) -> Unit) {
        GlobalScope.launch {
            stateMachine?.stopReverseOneAndResume(onDone)
        }
    }

    override fun deprecatedHandsomeBritish(shouldBeBritish: Boolean) {
        tts.setHandsomeBritish(shouldBeBritish)
    }

    override fun deprecatedSetSpeechRate(speechRate: Float) {
        tts.setSpeechRate(speechRate)
    }
}