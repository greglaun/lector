package com.greglaun.lector.android.bound

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.greglaun.lector.ui.speak.*
import kotlinx.coroutines.experimental.Deferred

class BindableTtsService : Service(), TtsStateMachine {
    // Binder given to clients
    private val mBinder = LocalBinder()

    private val delegateStateMachine = TtsActorStateMachine(JSoupArticleStateSource())

    override fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener) {
        delegateStateMachine.startMachine(ttsActorClient, stateListener)
    }

    override fun stopMachine() {
        delegateStateMachine.stopMachine()
    }

    override fun getState(): Deferred<SpeakerState> {
        return delegateStateMachine.getState()
    }

    override fun changeStateStopSpeakingNotReady(): Deferred<Unit> {
        return delegateStateMachine.changeStateStopSpeakingNotReady()
    }

    override fun changeStateReady(): Deferred<Unit> {
        return delegateStateMachine.changeStateReady()
    }

    override fun changeStateUpdateArticle(urlString: String, position: String): Deferred<Unit> {
        return delegateStateMachine.changeStateUpdateArticle(urlString, position)
    }

    override fun changeStateStartSpeaking(): Deferred<Unit> {
        return delegateStateMachine.changeStateStartSpeaking()
    }

    override fun actionSpeakOne(): Deferred<SpeakerState> {
        return delegateStateMachine.actionSpeakOne()
    }

    override fun actionStopSpeaking(): Deferred<Unit?> {
        return delegateStateMachine.actionStopSpeaking()
    }

    override fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit> {
        return delegateStateMachine.actionSpeakInLoop(onPositionUpdate)
    }

    override fun actionChangeUrl(urlString: String, position: String): Deferred<Unit> {
        return delegateStateMachine.actionChangeUrl(urlString, position)
    }

    override fun actionGetPosition(): Deferred<String> {
        return delegateStateMachine.actionGetPosition()
    }

    override fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return delegateStateMachine.stopAdvanceOneAndResume(onDone)
    }

    override fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit> {
        return delegateStateMachine.stopReverseOneAndResume(onDone)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BindableTtsService = this@BindableTtsService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}

