package com.greglaun.lector.ui.speak

class TtsPresenter(private val tts: TTSContract.AudioView)
    : TTSContract.Presenter, TtsActorClient {
    private var onArticleOver: (() -> Unit) = {}

    val stateMachine: TtsStateMachine? = TtsActorStateMachine()

    override fun speechViewSpeak(text: String, callback: (String) -> Unit) {
        synchronized(tts) {
            tts.speak(text) {
                callback(it)
            }
        }
    }

    override fun onStart() {
        stateMachine?.startMachine(this)
    }

    override fun onStop() {
        stateMachine?.stopMachine()
    }

    override fun speakInLoop() {
        stateMachine?.actionSpeakInLoop()
    }

    override fun onArticleOver() {
        onArticleOver?.invoke()
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override fun registerArticleOverCallback(onArticleOver: () -> Unit) {
        this.onArticleOver = onArticleOver
    }

    override fun onUrlChanged(urlString: String) {
        stateMachine?.actionChangeUrl(urlString)
    }

    override fun stopSpeaking() {
        stateMachine?.actionStopSpeaking()
    }
}

