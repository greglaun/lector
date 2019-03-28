package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

interface DeprecatedTtsStateMachine {
    fun attach(ttsView: TTSContract.AudioView,
               stateListener: TtsStateListener,
               store: Store)
}