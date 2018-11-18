package com.greglaun.lector.ui.speak

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun startSpeaking(onArticleOver: () -> Unit) {
        // Do nothing
        onArticleOver()
    }

    override fun stopSpeaking() {
        // Do nothing
    }

    override fun onUrlChanged(urlString: String) {
        // Do nothing
    }
}