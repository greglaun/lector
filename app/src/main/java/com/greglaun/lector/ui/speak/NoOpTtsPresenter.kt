package com.greglaun.lector.ui.speak

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun onStart(ttsStateListener: TtsStateListener) {
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

    override fun advanceOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }

    override fun reverseOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }
}