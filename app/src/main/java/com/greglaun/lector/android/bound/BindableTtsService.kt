package com.greglaun.lector.android.bound

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.ui.speak.*

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

    override suspend fun getSpeakerState(): SpeakerState {
        return delegateStateMachine!!.getSpeakerState()
    }

    override suspend fun updateArticle(articleState: ArticleState) {
        return delegateStateMachine!!.updateArticle(articleState)
    }

    override suspend fun actionSpeakOne(): SpeakerState {
        return delegateStateMachine!!.actionSpeakOne()
    }

    override suspend fun actionStopSpeaking() {
        return delegateStateMachine!!.actionStopSpeaking()
    }

    override suspend fun actionSpeakInLoop(onPositionUpdate: ((ArticleState) -> Unit)?) {
        return delegateStateMachine!!.actionSpeakInLoop(onPositionUpdate)
    }

    override suspend fun getArticleState(): ArticleState {
        return delegateStateMachine!!.getArticleState()
    }

    override suspend fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit) {
        return delegateStateMachine!!.stopAdvanceOneAndResume(onDone)
    }

    override suspend fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit) {
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

