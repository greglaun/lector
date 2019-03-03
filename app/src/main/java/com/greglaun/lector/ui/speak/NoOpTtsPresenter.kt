package com.greglaun.lector.ui.speak

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun onStart(ttsStateListener: TtsStateListener) {
        // Do nothing
    }

    override fun onStop() {
        // Do nothing
    }

    override suspend fun stopSpeaking() {
        // Do nothing
    }

    override suspend fun speakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        // Do nothing
    }

    override suspend fun onArticleChanged(articleState: ArticleState) {
        // Do nothing
    }

    override fun advanceOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }

    override fun reverseOne(onDone: (ArticleState) -> Unit) {
        // Do nothing
    }

    override fun setHandsomeBritish(shouldBeBritish: Boolean) {
        // Do nothing
    }

    override fun setSpeechRate(speechRate: Float) {
        // Do nothing
    }
}