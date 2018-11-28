package com.greglaun.lector.ui.speak

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun registerArticleOverCallback(onArticleOver: () -> Unit) {
        // Do nothing
    }

    override fun onStart() {
        // Do nothing
    }

    override fun onStop() {
        // Do nothing
    }

    override fun stopSpeaking() {
        // Do nothing
    }

    override fun onUrlChanged(urlString: String, position: String) {
        // Do nothing
    }

    override fun speakInLoop(onPositionUpdate: ((String) -> Unit)?) {
        // Do nothing
    }
}