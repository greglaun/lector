package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun attach(ttsView: TTSContract.AudioView?, store: Store) {
        // Do nothing
    }

    override fun ttsView(): TTSContract.AudioView? {
        return null
    }

    override suspend fun forwardOne() {
        // Do nothing
    }

    override suspend fun backOne() {
        // Do nothing
    }

    override fun stopImmediately() {
        // Do nothing
    }

    override fun deprecatedOnStop() {
        // Do nothing
    }

    override suspend fun stopSpeaking() {
        // Do nothing
    }

    override suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        // Do nothing
    }

    override suspend fun deprecatedOnArticleChanged(articleState: ArticleState) {
        // Do nothing
    }

    override fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }

    override fun deprecatedReverseOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }

    override fun deprecatedHandsomeBritish(shouldBeBritish: Boolean) {
        // Do nothing
    }

    override fun deprecatedSetSpeechRate(speechRate: Float) {
        // Do nothing
    }
}