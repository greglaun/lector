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

    override suspend fun stopSpeaking() {
        // Do nothing
    }

    override suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        // Do nothing
    }

}