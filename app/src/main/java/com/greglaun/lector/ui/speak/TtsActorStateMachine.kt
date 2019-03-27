package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

class TtsActorStateMachine : DeprecatedTtsStateMachine {
    private var store: Store? = null
    private var ttsStateListener: TtsStateListener? = null

    // Basic machine state
    override fun attach(ttsPresenter: TtsPresenter,
                        ttsStateListener: TtsStateListener,
                        store: Store) {
        this.store = store
        this.ttsStateListener = this.ttsStateListener
    }
}

