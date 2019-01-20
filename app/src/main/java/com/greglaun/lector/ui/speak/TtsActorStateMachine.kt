package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel

class TtsActorStateMachine : TtsStateMachine {
    private var ACTOR_LOOP: SendChannel<TtsMsg>? = null
    private val actorClient = newSingleThreadContext("ActorClient")
    var onPositionUpdate: ((String) -> Unit)? = null
    var SPEECH_LOOP: Job? = null

    override fun startMachine(ttsActorClient: TtsActorClient, ttsStateListener: TtsStateListener) {
        if (ACTOR_LOOP == null) {
            ACTOR_LOOP = ttsActor(ttsActorClient, ttsStateListener)
        }
    }

    override fun stopMachine() {
        ACTOR_LOOP?.close()
    }

    override suspend fun changeStateUpdateArticle(articleState: ArticleState)
            : Unit {
        ACTOR_LOOP?.send(UpdateArticleState(articleState))
    }


    override fun changeStateReady(): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            ACTOR_LOOP?.send(MarkReady)
            Unit
        }
    }

    override fun changeStateStartSpeaking(): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            ACTOR_LOOP?.send(StartSpeaking)
            Unit
        }
    }

    // todo(testing): Properly test this loop
    override fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit> {
        this.onPositionUpdate = onPositionUpdate
        SPEECH_LOOP =  CoroutineScope(actorClient).launch {
            var readyDeferred = getState()
            var timesDelayed = 0
            while(readyDeferred.await() != SpeakerState.READY) {
                // todo(concurrency): Ugly magical constants
                if (timesDelayed >= 50) {
                    actionStopSpeaking()
                    break
                }
                Thread.sleep(20)
                readyDeferred = getState()
                timesDelayed += 1
            }
            changeStateStartSpeaking()
            var stillSpeaking = true
            while(stillSpeaking) {
                var speakingState = actionSpeakOne()
                val position = actionGetPosition().await()
                this@TtsActorStateMachine.onPositionUpdate?.invoke(position)
                stillSpeaking = speakingState.await() == SpeakerState.SPEAKING
            }
        }
        SPEECH_LOOP?.start()
        return CompletableDeferred(Unit)
    }

    override fun actionSpeakOne(): Deferred<SpeakerState> {
        return CoroutineScope(actorClient).async {
            val speakingState = CompletableDeferred<SpeakerState>()
            ACTOR_LOOP?.send(SpeakOne(speakingState))
            speakingState.await()
        }
    }

    override fun getState(): Deferred<SpeakerState> {
        return CoroutineScope(actorClient).async {
            val stateDeferred = CompletableDeferred<SpeakerState>()
            ACTOR_LOOP?.send(GetSpeakerState(stateDeferred))
            stateDeferred.await()
        }
    }

    override fun actionGetPosition(): Deferred<String> {
        return CoroutineScope(actorClient).async {
            val positionDeferred = CompletableDeferred<String>()
            ACTOR_LOOP?.send(GetPosition(positionDeferred))
            positionDeferred.await()
        }
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return CoroutineScope(actorClient).async {
            ACTOR_LOOP?.send(StopSpeaking)
            SPEECH_LOOP?.isActive.let { SPEECH_LOOP?.cancel() }
            Unit
        }
    }

    override suspend fun actionChangeUrl(articleState: ArticleState) {
        actionStopSpeaking()
        changeStateUpdateArticle(articleState)
    }

    override fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val oldSpeakingState = getState()
            val newArticleState = CompletableDeferred<ArticleState>()
            ACTOR_LOOP?.send(ForwardOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
            changeStateReady().await()
            if (oldSpeakingState.await() == SpeakerState.SPEAKING) {
                actionSpeakInLoop { onPositionUpdate }.await()
            }
        }
    }

    override fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val oldSpeakingState = getState()
            val newArticleState = CompletableDeferred<ArticleState>()
            ACTOR_LOOP?.send(BackOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
            changeStateReady().await()
            if (oldSpeakingState.await() == SpeakerState.SPEAKING) {
                actionSpeakInLoop { onPositionUpdate }.await()
            }
        }
    }
}
