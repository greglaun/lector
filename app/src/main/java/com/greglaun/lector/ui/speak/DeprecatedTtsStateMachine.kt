package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerState
import com.greglaun.lector.store.Store

interface DeprecatedTtsStateMachine {
    fun attach(ttsActorClient: TtsActorClient, stateListener: TtsStateListener, store: Store)

    suspend fun getSpeakerState(): SpeakerState
    suspend fun actionStopSpeaking()
}