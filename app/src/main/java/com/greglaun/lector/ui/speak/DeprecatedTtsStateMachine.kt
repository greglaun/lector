package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

interface DeprecatedTtsStateMachine {
    fun attach(ttsActorClient: TtsActorClient, ttsPresenter: TtsPresenter,
               stateListener: TtsStateListener, store: Store)
}