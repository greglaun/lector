package com.greglaun.lector.android.bound

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.ui.speak.*
import kotlinx.coroutines.experimental.Deferred

class BindableTtsService : Service(), TtsStateMachine {
    private val binder = LocalBinder()

    private var delegateStateMachine: TtsActorStateMachine? = null

    // todo(error_handling): Remove ugly null assertions in this file
    override fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener) {
        this.delegateStateMachine!!.startMachine(ttsActorClient, stateListener)
    }

    override fun stopMachine() {
        delegateStateMachine!!.stopMachine()
    }

    override fun getState(): Deferred<SpeakerState> {
        return delegateStateMachine!!.getState()
    }

    override fun changeStateReady(): Deferred<Unit> {
        return delegateStateMachine!!.changeStateReady()
    }

    override fun changeStateStartSpeaking(): Deferred<Unit> {
        return delegateStateMachine!!.changeStateStartSpeaking()
    }

    override suspend fun changeStateUpdateArticle(articleState: ArticleState) {
        return delegateStateMachine!!.changeStateUpdateArticle(articleState)
    }

    override suspend fun actionChangeUrl(articleState: ArticleState) {
        return delegateStateMachine!!.actionChangeUrl(articleState)
    }

    override fun actionSpeakOne(): Deferred<SpeakerState> {
        return delegateStateMachine!!.actionSpeakOne()
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return delegateStateMachine!!.actionStopSpeaking()
    }

    override fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit> {
        return delegateStateMachine!!.actionSpeakInLoop(onPositionUpdate)
    }

    override fun actionGetPosition(): Deferred<String> {
        return delegateStateMachine!!.actionGetPosition()
    }

    override fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return delegateStateMachine!!.stopAdvanceOneAndResume(onDone)
    }

    override fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return delegateStateMachine!!.stopReverseOneAndResume(onDone)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(responseSource: ResponseSource): BindableTtsService {
            if (responseSource == null) {
                throw RuntimeException("Must have a valid responseSource.")
            }
            delegateStateMachine = TtsActorStateMachine()
            return this@BindableTtsService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}

