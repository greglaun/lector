package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.SpeakerState
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.UpdateAction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel

class TtsActorStateMachine : DeprecatedTtsStateMachine {
    internal var ACTOR_LOOP: SendChannel<TtsMsg>? = null
    internal var SPEECH_LOOP: Job? = null
    private val actorClient = newSingleThreadContext("ActorClient")
    private var onPositionUpdate: ((ArticleState) -> Unit)? = null
    private var store: Store? = null
    private var ttsStateListener: TtsStateListener? = null
    var ttsClient: TtsActorClient? = null

    // Basic machine state

    override fun attach(ttsActorClient: TtsActorClient,
                        ttsStateListener: TtsStateListener,
                        store: Store) {
        this.store = store
        this.ttsStateListener = this.ttsStateListener
        ttsClient = ttsActorClient
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
        store?.dispatch(SpeakerAction.SpeakAction())
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
        store?.dispatch(SpeakerAction.StopSpeakingAction())
        ttsStateListener?.onSpeechStopped()
    }

    // Transport

   suspend fun forwardOne() {
        store?.let {
           store!!.dispatch(UpdateAction.FastForwardOne())
           if (store!!.state.currentArticleScreen.articleState.hasNext()) {
               ttsClient?.stopSpeechViewImmediately()
           }
       }
   }

    suspend fun backOne() {
        store?.let {
            store!!.dispatch(UpdateAction.RewindOne())
            if (store!!.state.currentArticleScreen.articleState.hasPrevious()) {
                ttsClient?.stopSpeechViewImmediately()
            }
        }
    }

    override suspend fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit) {
        val oldSpeakingState = getSpeakerState()
        forwardOne()
        onDone(getArticleState())
        if (oldSpeakingState == SpeakerState.SPEAKING) {
            actionSpeakInLoop { onPositionUpdate }
        }
    }

    override suspend fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit) {
        val oldSpeakingState = getSpeakerState()
        backOne()
        onDone(getArticleState())
        if (oldSpeakingState == SpeakerState.SPEAKING) {
            actionSpeakInLoop { onPositionUpdate }
        }
    }
}

