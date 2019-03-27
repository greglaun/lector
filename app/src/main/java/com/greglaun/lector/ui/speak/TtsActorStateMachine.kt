package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

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
}

