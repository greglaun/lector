package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

interface DeprecatedTtsStateMachine {
    fun attach(ttsPresenter: TtsPresenter,
               ttsView: TTSContract.AudioView,
               stateListener: TtsStateListener,
               store: Store)
}