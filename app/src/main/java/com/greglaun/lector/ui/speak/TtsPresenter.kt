package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.UpdateAction

class TtsPresenter(val ttsView: TTSContract.AudioView,
                   val stateMachine: DeprecatedTtsStateMachine,
                   val store: Store)
    : TTSContract.Presenter, TtsActorClient {

    override fun ttsView(): TTSContract.AudioView? {
        return ttsView
    }


    override fun stopImmediately() {
        ttsView.stopImmediately()
    }

    override suspend fun forwardOne() {
        store?.let {
            store!!.dispatch(UpdateAction.FastForwardOne())
            if (store!!.state.currentArticleScreen.articleState.hasNext()) {
                ttsView.stopImmediately()
            }
        }
    }

    override suspend fun backOne() {
        store?.let {
            store!!.dispatch(UpdateAction.RewindOne())
            if (store!!.state.currentArticleScreen.articleState.hasPrevious()) {
                ttsView.stopImmediately()
            }
        }
    }

    override fun deprecatedOnStart(stateListener: TtsStateListener) {
        stateMachine?.attach(this, stateListener, store)
    }

    override fun deprecatedOnStop() {}

    override suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        store?.dispatch(SpeakerAction.SpeakAction())
    }

    override suspend fun deprecatedOnArticleChanged(articleState: ArticleState) {
    }

    override suspend fun stopSpeaking() {
        store?.dispatch(SpeakerAction.StopSpeakingAction())
    }

    override fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit) {
    }

    override fun deprecatedReverseOne(onDone: (ArticleState) -> Unit) {
    }

    override fun deprecatedHandsomeBritish(shouldBeBritish: Boolean) {
        ttsView.setHandsomeBritish(shouldBeBritish)
    }

    override fun deprecatedSetSpeechRate(speechRate: Float) {
        ttsView.setSpeechRate(speechRate)
    }
}