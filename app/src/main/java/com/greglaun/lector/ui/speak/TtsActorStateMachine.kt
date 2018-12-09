package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel

class TtsActorStateMachine(val articleStateSource: ArticleStateSource) : TtsStateMachine {
    private var actorLoop: SendChannel<TtsMsg>? = null
    private val actorClient = newSingleThreadContext("ActorClient")

    override fun startMachine(ttsActorClient: TtsActorClient, ttsStateListener: TtsStateListener) {
        actorLoop = ttsActor(ttsActorClient, ttsStateListener)
    }

    override fun stopMachine() {
        actorLoop?.close()
    }

    override fun changeStateStopSpeakingNotReady(): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            actorLoop?.send(StopSeakingAndMarkNotReady)
            Unit
        }
    }

    override fun changeStateUpdateArticle(urlString: String,
                                          position: String)
            : Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val articleState = articleStateSource.getArticle(urlString)
            actorLoop?.send(UpdateArticleState(articleState, position))
            Unit
        }
    }

    override fun changeStateReady(): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            actorLoop?.send(MarkReady)
            Unit
        }
    }

    override fun changeStateStartSpeaking(): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            actorLoop?.send(StartSpeaking)
            Unit
        }
    }

    // todo(testing): Properly test this loop
    override fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            var readyDeferred = getState()
            while(readyDeferred.await() != SpeakerState.READY) {
                Thread.sleep(20)
                readyDeferred = getState()
            }
            changeStateStartSpeaking()
            var stillSpeaking = true
            while(stillSpeaking) {
                var speakingState = actionSpeakOne()
                val position = actionGetPosition().await()
                onPositionUpdate?.invoke(position)
                while (speakingState.await() == SpeakerState.SCRUBBING) {
                    Thread.sleep(10)
                    speakingState = getState()
                }
                stillSpeaking = speakingState.await() == SpeakerState.SPEAKING
            }
        }
    }

    override fun actionSpeakOne(): Deferred<SpeakerState> {
        return CoroutineScope(actorClient).async {
            val speakingState = CompletableDeferred<SpeakerState>()
            actorLoop?.send(SpeakOne(speakingState))
            speakingState.await()
        }
    }

    override fun getState(): Deferred<SpeakerState> {
        return CoroutineScope(actorClient).async {
            val stateDeferred = CompletableDeferred<SpeakerState>()
            actorLoop?.send(GetSpeakerState(stateDeferred))
            stateDeferred.await()
        }
    }

    override fun actionGetPosition(): Deferred<String> {
        return CoroutineScope(actorClient).async {
            val positionDeferred = CompletableDeferred<String>()
            actorLoop?.send(GetPosition(positionDeferred))
            positionDeferred.await()
        }
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return CoroutineScope(actorClient).async {
            actorLoop?.send(StopSpeaking)
        }
    }

    override fun actionChangeUrl(urlString: String, position: String): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            changeStateStopSpeakingNotReady().await()
            changeStateUpdateArticle(urlString, position).await()
        }
    }

    override fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val newArticleState = CompletableDeferred<ArticleState>()
            actorLoop?.send(ForwardOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
        }
    }

    override fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val newArticleState = CompletableDeferred<ArticleState>()
            actorLoop?.send(BackOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
        }
    }
}
