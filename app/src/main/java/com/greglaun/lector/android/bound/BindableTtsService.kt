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
    private var store: Store? = null
    private var delegateStateMachine: TtsActorStateMachine? = null
    private var ttsView: TTSContract.AudioView? = null

    // todo(error_handling): Remove ugly null assertions in this file
    override fun attach(ttsView: TTSContract.AudioView?,
                        store: Store) {
        store?.stateHandlers?.add(this)
        this.ttsView = ttsView
        this.store = store
    }


    override suspend fun handle(state: State) {
        handleState(state)
    }

    private suspend fun handleState(state: State) {
        if (state.preferenceChanged) {
            handlePreferenceChanged(state)
            return
        }
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
            ttsView?.speak(cleanUtterance(text),
                    utteranceId(text)) {
                        if (it == utteranceId(text)) {
                            if (articleState!!.hasNext()) {
                                store?.dispatch(UpdateAction.FastForwardOne())

                            } else {
                                store?.dispatch(UpdateAction.ArticleOverAction())
                            }
                        }
            }

        }
   }

    fun handlePreferenceChanged(state: State) {
        ttsView?.setHandsomeBritish(state.preferences.isBritish)
        if (state.preferences.isSlow) {
            ttsView?.setSpeechRate(1.0f)
        } else {
            ttsView?.setSpeechRate(state.preferences.speechRate)
        }
    }

    fun detach() {
        store?.stateHandlers?.remove(this)
    }

    override fun stopImmediately() {
       ttsView?.stopImmediately()
    }
    override suspend fun forwardOne() {
        store?.let {
            store!!.dispatch(UpdateAction.FastForwardOne())
            if (store!!.state.currentArticleScreen.articleState.hasNext()) {
                ttsView?.stopImmediately()
            }
        }
    }

    override suspend fun backOne() {
        store?.let {
            store!!.dispatch(UpdateAction.RewindOne())
            if (store!!.state.currentArticleScreen.articleState.hasPrevious()) {
                ttsView?.stopImmediately()
            }
        }
    }

    override suspend fun startSpeaking(onPositionUpdate: ((AbstractArticleState) -> Unit)?) {
        store?.dispatch(SpeakerAction.SpeakAction())
    }

    override suspend fun stopSpeaking() {
        store?.dispatch(SpeakerAction.StopSpeakingAction())
    }

    override fun ttsView(): TTSContract.AudioView? {
        return ttsView
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

