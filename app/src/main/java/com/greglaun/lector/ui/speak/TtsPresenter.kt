package com.greglaun.lector.ui.speak

class TtsPresenter(private val tts: TTSContract.AudioView,
                   val stateMachine: TtsStateMachine)
    : TTSContract.Presenter, TtsActorClient {
    private var onArticleOver: (() -> Unit) = {}

    override fun speechViewSpeak(text: String, callback: (String) -> Unit) {
        synchronized(tts) {
            tts.speak(text) {
                callback(it)
            }
        }
    }

    override fun onStart(onArticleOver: () -> Unit) {
        this.onArticleOver = onArticleOver
        stateMachine?.startMachine(this)
    }

    override fun onStop() {
        stateMachine?.stopMachine()
    }

    override fun speakInLoop(onPositionUpdate: ((String) -> Unit)?) {
        stateMachine?.actionSpeakInLoop(onPositionUpdate)
    }

    override fun onArticleOver() {
        onArticleOver?.invoke()
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override fun onUrlChanged(urlString: String, postition: String) {
        stateMachine?.actionChangeUrl(urlString, position = postition)
    }

    override fun stopSpeaking() {
        stateMachine?.actionStopSpeaking()
    }
}

