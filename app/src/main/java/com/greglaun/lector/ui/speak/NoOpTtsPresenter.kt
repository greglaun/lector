package com.greglaun.lector.ui.speak

class NoOpTtsPresenter : TTSContract.Presenter {
    override fun deprecatedOnStart(ttsStateListener: TtsStateListener) {
        // Do nothing
    }

    override fun deprecatedOnStop() {
        // Do nothing
    }

    override suspend fun deprecatedStopSpeaking() {
        // Do nothing
    }

    override suspend fun deprecatedSpeakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
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