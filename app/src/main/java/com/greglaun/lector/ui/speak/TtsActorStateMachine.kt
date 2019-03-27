package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.SpeakerState
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.UpdateAction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel

class TtsActorStateMachine : DeprecatedTtsStateMachine {
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
    }


    override suspend fun getSpeakerState(): SpeakerState {
        return store!!.state.speakerState
    }

    override suspend fun actionStopSpeaking() {
        store?.dispatch(SpeakerAction.StopSpeakingAction())
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

}

