package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.UpdateAction
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

    override suspend fun forwardOne() {
        store?.let {
            store!!.dispatch(UpdateAction.FastForwardOne())
            if (store!!.state.currentArticleScreen.articleState.hasNext()) {
                stopSpeechViewImmediately()
            }
        }
    }

    override suspend fun backOne() {
        store?.let {
            store!!.dispatch(UpdateAction.RewindOne())
            if (store!!.state.currentArticleScreen.articleState.hasPrevious()) {
                stopSpeechViewImmediately()
            }
        }
    }

    override fun deprecatedOnStart(stateListener: TtsStateListener) {
        stateMachine?.attach(this, stateListener, store)
    }

    override fun deprecatedOnStop() {}

    override suspend fun deprecatedSpeakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        store?.dispatch(SpeakerAction.SpeakAction())
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override suspend fun deprecatedOnArticleChanged(articleState: ArticleState) {
    }

    override suspend fun deprecatedStopSpeaking() {
        store?.dispatch(SpeakerAction.StopSpeakingAction())
    }

    override fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit) {
    }

    override fun deprecatedReverseOne(onDone: (ArticleState) -> Unit) {
    }

    override fun deprecatedHandsomeBritish(shouldBeBritish: Boolean) {
        tts.setHandsomeBritish(shouldBeBritish)
    }

    override fun deprecatedSetSpeechRate(speechRate: Float) {
        tts.setSpeechRate(speechRate)
    }
}