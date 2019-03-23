package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel

class TtsActorStateMachine : DeprecatedTtsStateMachine {
    internal var ACTOR_LOOP: SendChannel<TtsMsg>? = null
    internal var SPEECH_LOOP: Job? = null
    private val actorClient = newSingleThreadContext("ActorClient")
    private var onPositionUpdate: ((ArticleState) -> Unit)? = null
    private var store: Store? = null
    private var ttsListener: TtsStateListener? = null

    // Basic machine state

    override fun attach(ttsActorClient: TtsActorClient,
                        ttsStateListener: TtsStateListener,
                        store: Store) {
        this.store = store
        this.ttsListener = ttsListener
        if (ACTOR_LOOP == null) {
            ACTOR_LOOP = ttsActor(ttsActorClient, ttsStateListener, store)
        }
    }

    override fun detach() {
        ACTOR_LOOP?.close()
    }

    // Article State

    override suspend fun updateArticle(articleState: ArticleState) {
        actionStopSpeaking()
        ACTOR_LOOP?.send(MarkReady(articleState))
    }

    override suspend fun getArticleState(): ArticleState {
        return store!!.state.currentArticleScreen.articleState as ArticleState
    }

    // Speaking state

    override suspend fun actionSpeakInLoop(onPositionUpdate: ((ArticleState) -> Unit)?) {
        this.onPositionUpdate = onPositionUpdate
        SPEECH_LOOP =  CoroutineScope(actorClient).launch {
            var readyDeferred = getSpeakerState()
            var timesDelayed = 0
            while(readyDeferred != SpeakerState.READY
                    && readyDeferred != SpeakerState.SPEAKING) {
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
                actionSpeakOne()
                var speakingState = getSpeakerState()
                val articleState = getArticleState()
                articleState.current()?.also {
                    this@TtsActorStateMachine.onPositionUpdate?.invoke(articleState)
                }
                stillSpeaking = speakingState == SpeakerState.SPEAKING
            }
        }
        SPEECH_LOOP?.start()
    }

    override suspend fun actionSpeakOne(): SpeakerState {
        val speakingState = CompletableDeferred<SpeakerState>()
        ACTOR_LOOP?.send(SpeakOne(speakingState))
        return speakingState.await()
    }

    override suspend fun getSpeakerState(): SpeakerState {
        return store!!.state.speakerState
    }

    override suspend fun actionStopSpeaking() {
        ACTOR_LOOP?.send(StopSpeaking)
        SPEECH_LOOP?.isActive.let { SPEECH_LOOP?.cancel() }
    }

    // Transport

    override suspend fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit) {
        val oldSpeakingState = getSpeakerState()
        val newArticleState = CompletableDeferred<ArticleState>()
        ACTOR_LOOP?.send(TTSForwardOne(newArticleState = newArticleState))
        newArticleState.await() // todo: Useless, delete after refactoring
        onDone(getArticleState())
        if (oldSpeakingState == SpeakerState.SPEAKING) {
            actionSpeakInLoop { onPositionUpdate }
        }
    }

    override suspend fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit) {
        val oldSpeakingState = getSpeakerState()
        val newArticleState = CompletableDeferred<ArticleState>()
        ACTOR_LOOP?.send(TTSBackOne(newArticleState = newArticleState))
        newArticleState.await() // todo: Useless, delete after refactoring
        onDone(getArticleState())
        if (oldSpeakingState == SpeakerState.SPEAKING) {
            actionSpeakInLoop { onPositionUpdate }
        }
    }
}
