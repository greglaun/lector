package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel

class TtsActorStateMachine(val articleStateSource: ArticleStateSource) : TtsStateMachine {
    private var actorLoop: SendChannel<TtsMsg>? = null
    private val workerContext = newFixedThreadPoolContext(2, "WorkerContext")

    override fun startMachine(ttsActorClient: TtsActorClient) {
        actorLoop = ttsActor(ttsActorClient)
    }

    override fun stopMachine() {
        actorLoop?.close()
    }

    override fun changeStateNotReady(): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            actorLoop?.send(MarkNotReady)
            Unit
        }
    }

    override fun changeStateUpdateArticle(urlString: String): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            val articleState = articleStateSource.getArticle(urlString)
            actorLoop?.send(UpdateArticleState(articleState))
            Unit
        }
    }

    override fun changeStateReady(): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            actorLoop?.send(MarkReady)
            Unit
        }
    }

    override fun changeStateStartSpeaking(): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            actorLoop?.send(StartSpeaking)
            Unit
        }
    }

    // todo(testing): Properly test this loop
    override fun actionSpeakInLoop(): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            var readyStatus = false
            while(!readyStatus) {
                val readyDeferred = getState()
                if (readyDeferred.await() == SpeakerState.READY) {
                    readyStatus = true
                } else {
                    delay(250)
                }
            }
            changeStateStartSpeaking()
            var stillSpeaking = true
            while(stillSpeaking) {
                val speakingState = actionSpeakOne()
                stillSpeaking = speakingState.await() == SpeakerState.SPEAKING
            }
        }
    }

    override fun actionSpeakOne(): Deferred<SpeakerState> {
        return CoroutineScope(workerContext).async {
            val speakingState = CompletableDeferred<SpeakerState>()
            actorLoop?.send(SpeakOne(speakingState))
            speakingState.await()
        }
    }

    override fun getState(): Deferred<SpeakerState> {
        return CoroutineScope(workerContext).async {
            val stateDeferred = CompletableDeferred<SpeakerState>()
            actorLoop?.send(GetSpeakerState(stateDeferred))
            stateDeferred.await()
        }
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return CoroutineScope(workerContext).async {
            actorLoop?.send(StopSpeaking)
        }
    }

    override fun actionChangeUrl(urlString: String): Deferred<Unit> {
        return CoroutineScope(workerContext).async {
            changeStateNotReady().await()
            changeStateUpdateArticle(urlString).await()
            changeStateReady().await()
        }
    }


}
