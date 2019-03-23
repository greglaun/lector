package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.Store
import com.greglaun.lector.store.UpdateAction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

private val actorContext = newSingleThreadContext("ActorContext")

fun ttsActor(ttsClient: TtsActorClient, ttsStateListener: TtsStateListener, store: Store) =
        CoroutineScope(actorContext).actor<TtsMsg>(
                Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
    for (msg in channel) {
        when (msg) {
            is TTSBackOne -> {
                store.dispatch(UpdateAction.RewindOne())
                ttsStateListener.onUtteranceEnded(
                        store.state.currentArticleScreen.articleState!! as ArticleState)
                if (store.state.currentArticleScreen.articleState.hasPrevious()) {
                    ttsClient.stopSpeechViewImmediately()
                }
            }
            // todo
            is SpeakOne -> {
                if (store.state.speakerState != SpeakerState.SPEAKING) {
                    store.dispatch(UpdateAction.UpdateSpeakerStateAction(SpeakerState.SPEAKING))
                    // todo: Delete this hack after refactoring is finished
                    while (store.state.speakerState != SpeakerState.SPEAKING) {
                        delay(10)
                    }
                }
                val articleState = store.state.currentArticleScreen.articleState as ArticleState
                ttsStateListener.onUtteranceStarted(articleState!!)
                var text = articleState!!.current()!!
                ttsClient.speechViewSpeak(cleanUtterance(text), utteranceId(text)) {
                    if (it == utteranceId(text)) {
                            ttsStateListener.onUtteranceEnded(articleState!!)
                        if (articleState!!.hasNext()) {
                            runBlocking {
                                store.dispatch(UpdateAction.FastForwardOne())
                            }
                        } else {
                            runBlocking {
                                    store.dispatch(UpdateAction.UpdateSpeakerStateAction(
                                            SpeakerState.NOT_READY))
                                ttsStateListener.onSpeechStopped()
                                ttsStateListener.onArticleFinished(articleState!!)
                            }
                        }
                    }
                }
            }
        }
    }
})

enum class SpeakerState {
    NOT_READY,
    READY,
    SCRUBBING,
    SPEAKING
}

// Message types for ttsActor
sealed class TtsMsg
class SpeakOne(val speakerState: CompletableDeferred<SpeakerState>) : TtsMsg()

@Deprecated("Soon to be removed")
class UpdateArticleStateDeprecated(val articleState: ArticleState, val position: String): TtsMsg()
class MarkReady(val articleState: ArticleState): TtsMsg()

class TTSBackOne(val newArticleState: CompletableDeferred<ArticleState>): TtsMsg()
