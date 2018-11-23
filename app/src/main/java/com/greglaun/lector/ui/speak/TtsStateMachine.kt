package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.Deferred

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient)
    fun stopMachine()

    fun getState(): Deferred<SpeakerState>

    fun changeStateNotReady(): Deferred<Unit>
    fun changeStateReady(): Deferred<Unit>
    fun changeStateUpdateArticle(urlString: String): Deferred<Unit>
    fun changeStateStartSpeaking(): Deferred<Unit>

    fun actionSpeakOne(): Deferred<SpeakerState>
    fun actionStopSpeaking(): Deferred<Unit?>
    fun actionSpeakInLoop(): Deferred<Unit>
    fun actionChangeUrl(urlString: String): Deferred<Unit>
}