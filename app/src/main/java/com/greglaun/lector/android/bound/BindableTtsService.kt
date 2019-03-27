package com.greglaun.lector.android.bound

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.*

class BindableTtsService : Service(), DeprecatedTtsStateMachine, TTSContract.Presenter,
        StateHandler {
    private val binder = LocalBinder()
    private var ttsPresenter: TtsPresenter? = null
    private var ttsStateListener: TtsStateListener? = null
    private var store: Store? = null
    private var delegateStateMachine: TtsActorStateMachine? = null

    // todo(error_handling): Remove ugly null assertions in this file
    override fun attach(ttsPresenter: TtsPresenter,
                        stateListener: TtsStateListener,
                        store: Store) {
        this.ttsPresenter = ttsPresenter
        ttsStateListener = stateListener
        ttsPresenter?.store?.stateHandlers?.add(this)
        this.store = store
        this.delegateStateMachine!!.attach(ttsPresenter!!, stateListener, store)
    }


    override suspend fun handle(state: State) {
        handleState(state)
    }

    private suspend fun handleState(state: State) {
        if (state.speakerState != SpeakerState.SPEAKING &&
                state.speakerState != SpeakerState.SPEAKING_NEW_UTTERANCE) {
          stopImmediately()
        }
        if (state.speakerState == SpeakerState.SPEAKING_NEW_UTTERANCE) {
            startSpeaking(state)
        }
    }

   suspend fun startSpeaking(state: State) {
        val articleState = state.currentArticleScreen.articleState
        articleState.current()?.let {text ->
            ttsPresenter?.ttsView?.speak(cleanUtterance(text),
                    utteranceId(text)) {
                        if (it == utteranceId(text)) {
            if (articleState!!.hasNext()) {
                store?.dispatch(UpdateAction.FastForwardOne())

            } else {
                    store?.dispatch(UpdateAction.UpdateSpeakerStateAction(
                            SpeakerState.NOT_READY))
                    ttsStateListener?.onArticleFinished(articleState!! as ArticleState)
                }
            }
        }

        }
    }

    fun detach() {
        ttsPresenter?.store?.stateHandlers?.remove(this)
    }

    override fun stopImmediately() {
        ttsPresenter?.stopImmediately()
    }
    override suspend fun forwardOne() {
        ttsPresenter?.forwardOne()
    }

    override suspend fun backOne() {
        ttsPresenter?.backOne()
    }

    override suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun stopSpeaking() {
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

    override fun ttsView(): TTSContract.AudioView? {
        return ttsPresenter?.ttsView
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

