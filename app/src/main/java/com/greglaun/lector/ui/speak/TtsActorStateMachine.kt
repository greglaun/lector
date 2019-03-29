package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

class TtsActorStateMachine : DeprecatedTtsStateMachine {
    private var store: Store? = null

    // Basic machine state
    override fun attach(ttsView: TTSContract.AudioView,
                        store: Store) {
        this.store = store
    }
}

