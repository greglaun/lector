package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.POSITION_BEGINNING
import kotlinx.coroutines.experimental.Deferred

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener)
    fun stopMachine()

    fun getState(): Deferred<SpeakerState>

    fun changeStateStopSpeaking(): Deferred<Unit>
    fun changeStateReady(): Deferred<Unit>
    fun changeStateUpdateArticle(urlString: String, position: String = POSITION_BEGINNING)
            : Deferred<Unit>
    fun changeStateStartSpeaking(): Deferred<Unit>

    fun actionSpeakOne(): Deferred<SpeakerState>
    fun actionStopSpeaking(): Deferred<Unit?>
    fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit>
    fun actionChangeUrl(urlString: String, position: String = POSITION_BEGINNING): Deferred<Unit>
    fun actionGetPosition(): Deferred<String>

    fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
    fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
}