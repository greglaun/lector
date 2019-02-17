package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel

class TtsActorStateMachine : TtsStateMachine {
    internal var ACTOR_LOOP: SendChannel<TtsMsg>? = null
    internal var SPEECH_LOOP: Job? = null
    private val actorClient = newSingleThreadContext("ActorClient")
    private var onPositionUpdate: ((String) -> Unit)? = null

    // Basic machine state

    override fun startMachine(ttsActorClient: TtsActorClient, ttsStateListener: TtsStateListener) {
        if (ACTOR_LOOP == null) {
            ACTOR_LOOP = ttsActor(ttsActorClient, ttsStateListener)
        }
    }

    override fun stopMachine() {
        ACTOR_LOOP?.close()
    }

    // Article State

    override suspend fun updateArticle(articleState: ArticleState) {
        actionStopSpeaking()
        ACTOR_LOOP?.send(UpdateArticleState(articleState))
    }

    override fun getArticleState(): Deferred<ArticleState> {
        return CoroutineScope(actorClient).async {
            val articleStateDeferred = CompletableDeferred<ArticleState>()
            ACTOR_LOOP?.send(GetArticleState(articleStateDeferred))
            articleStateDeferred.await()
        }
    }

    // Speaking state

    override fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit> {
        this.onPositionUpdate = onPositionUpdate
        SPEECH_LOOP =  CoroutineScope(actorClient).launch {
            var readyDeferred = getSpeakerState()
            var timesDelayed = 0
            while(readyDeferred.await() != SpeakerState.READY
                    && readyDeferred.await() != SpeakerState.SPEAKING) {
                // todo(concurrency): Ugly magical constants
                if (timesDelayed >= 50) {
                    actionStopSpeaking()
                    break
                }
                Thread.sleep(20)
                readyDeferred = getSpeakerState()
                timesDelayed += 1
            }
            var stillSpeaking = true
            while(stillSpeaking) {
                var speakingState = actionSpeakOne()
                val articleState = getArticleState().await()
                articleState.current()?.also {
                    this@TtsActorStateMachine.onPositionUpdate?.invoke(utteranceId(it))
                }
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

    override fun getSpeakerState(): Deferred<SpeakerState> {
        return CoroutineScope(actorClient).async {
            val stateDeferred = CompletableDeferred<SpeakerState>()
            ACTOR_LOOP?.send(GetSpeakerState(stateDeferred))
            stateDeferred.await()
        }
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return CoroutineScope(actorClient).async {
            ACTOR_LOOP?.send(StopSpeaking)
            SPEECH_LOOP?.isActive.let { SPEECH_LOOP?.cancel() }
            Unit
        }
    }

    // Transport

    override fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val oldSpeakingState = getSpeakerState()
            val newArticleState = CompletableDeferred<ArticleState>()
            ACTOR_LOOP?.send(ForwardOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
            if (oldSpeakingState.await() == SpeakerState.SPEAKING) {
                actionSpeakInLoop { onPositionUpdate }.await()
            }
        }
    }

    override fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return CoroutineScope(actorClient).async {
            val oldSpeakingState = getSpeakerState()
            val newArticleState = CompletableDeferred<ArticleState>()
            ACTOR_LOOP?.send(BackOne(newArticleState = newArticleState))
            onDone(newArticleState.await())
            if (oldSpeakingState.await() == SpeakerState.SPEAKING) {
                actionSpeakInLoop { onPositionUpdate }.await()
            }
        }
    }
}
