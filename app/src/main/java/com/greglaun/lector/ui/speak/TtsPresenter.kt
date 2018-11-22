package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel

class TtsPresenter(private val tts: TTSContract.AudioView)
    : TTSContract.Presenter, TtsActorClient {
    private var actorLoop: SendChannel<TtsMsg>? = null
    private val workerContext = newFixedThreadPoolContext(2, "WorkerContext")
    private var onArticleOver: (() -> Unit) = {}

    override fun speechViewSpeak(text: String, callback: (String) -> Unit) {
        synchronized(tts) {
            tts.speak(text) {
                callback(it)
            }
        }
    }

    override fun onArticleOver() {
        onArticleOver?.invoke()
    }

    override fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

    override fun onStart() {
        actorLoop = ttsActor(this)
    }

    override fun onStop() {
        actorLoop?.close()
    }

    override fun registerArticleOverCallback(onArticleOver: () -> Unit) {
        this.onArticleOver = onArticleOver
    }

    override fun onUrlChanged(urlString: String) {
        CoroutineScope(workerContext).launch {
            actorLoop?.send(MarkNotReady)
            val articleState = jsoupStateFromUrl(urlString)
            actorLoop?.send(UpdateArticleState(articleState))
            actorLoop?.send(MarkReady)
        }
    }

    override fun startSpeaking() {
        CoroutineScope(workerContext).launch {
            var readyStatus = false
            while(!readyStatus) {
                val readyDeferred = CompletableDeferred<SpeakerState>()
                actorLoop?.send(GetSpeakerState(readyDeferred))
                if (readyDeferred.await() == SpeakerState.READY) {
                    readyStatus = true
                } else {
                    delay(250)
                }
            }
            actorLoop?.send(StartSpeaking)
            var stillSpeaking = true
            while(stillSpeaking) {
                val speakingState = CompletableDeferred<SpeakerState>()
                actorLoop?.send(SpeakOne(speakingState))
                stillSpeaking = speakingState.await() == SpeakerState.SPEAKING
            }
        }
    }

    override fun stopSpeaking() {
        CoroutineScope(workerContext).launch {
            actorLoop?.send(StopSpeaking)
        }
    }
}

