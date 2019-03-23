package com.greglaun.lector.android.bound

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.store.Navigation
import com.greglaun.lector.store.State
import com.greglaun.lector.store.StateHandler
import com.greglaun.lector.store.Store
import com.greglaun.lector.ui.speak.*

class BindableTtsService : Service(), DeprecatedTtsStateMachine, TTSContract.Presenter,
        StateHandler {
    private val binder = LocalBinder()
    private var ttsPresenter: TtsPresenter? = null

    private var delegateStateMachine: TtsActorStateMachine? = null

    // todo(error_handling): Remove ugly null assertions in this file
    override fun startMachine(ttsActorClient: TtsActorClient,
                              stateListener: TtsStateListener,
                              store: Store) {
        ttsPresenter = ttsActorClient as TtsPresenter
        this.delegateStateMachine!!.startMachine(ttsActorClient, stateListener, store)
    }


    override suspend fun handle(state: State) {
//        if (isBound()) // Figure out how to tell this {
            handleState(state)
//        }
    }

    private fun handleState(state: State) {
        if (state.speakerState != SpeakerState.SPEAKING) {
          stopImmediately()
        }
    }


    override fun stopMachine() {
        delegateStateMachine!!.stopMachine()
    }

    override fun stopImmediately() {
        ttsPresenter?.stopImmediately()
    }

    override suspend fun deprecatedSpeakInLoop(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deprecatedStopSpeaking() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deprecatedOnArticleChanged(articleState: ArticleState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedOnStart(stateListener: TtsStateListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedOnStop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedAdvanceOne(onDone: (ArticleState) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedReverseOne(onDone: (ArticleState) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedHandsomeBritish(shouldBeBritish: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deprecatedSetSpeechRate(speechRate: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

