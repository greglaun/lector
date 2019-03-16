package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.store.Store
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

private val actorContext = newSingleThreadContext("ActorContext")

fun ttsActor(ttsClient: TtsActorClient, ttsStateListener: TtsStateListener, store: Store) =
        CoroutineScope(actorContext).actor<TtsMsg>(
                Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
    var articleState: ArticleState? = null
    var state = SpeakerState.NOT_READY
    for (msg in channel) {
        when (msg) {
            is TTSUpdateArticleState -> {
                state = SpeakerState.NOT_READY
                articleState = msg.articleState
                synchronized(state) {
                    state = SpeakerState.READY
                }
            }
            is StopSeakingAndMarkNotReady -> {
                ttsClient.stopSpeechViewImmediately()
                ttsStateListener.onSpeechStopped()
            }
            is GetSpeakerState -> msg.response.complete(state)
            is StopSpeaking -> {
                val previousState = state
                ttsClient.stopSpeechViewImmediately()
                if (previousState == SpeakerState.SPEAKING) {
                    state = SpeakerState.READY
                } else {
                    state = previousState
                }
            }
            is TTSForwardOne -> {
                if (articleState != null && articleState.currentIndex() != null &&
                        articleState.hasNext()) {
                    val initialState = state
                    ttsClient.stopSpeechViewImmediately()
                    state = SpeakerState.SCRUBBING
                    ttsClient.stopSpeechViewImmediately()
                    articleState = articleState.next()
                    state = initialState
                }
                msg.newArticleState.complete(articleState!!)
            }
            is TTSBackOne -> {
                if (articleState != null && articleState.currentPosition != null &&
                        articleState.hasPrevious()) {
                    val initalState = state
                    ttsClient.stopSpeechViewImmediately()
                    state = SpeakerState.SCRUBBING
                    ttsClient.stopSpeechViewImmediately()
                    articleState = articleState.previous()
                    state = initalState
                }
                msg.newArticleState.complete(articleState!!)
            }
            is TTSGetArticleState -> {
                msg.articleState.complete(articleState!!)
            }
            is SpeakOne -> {
                if (state == SpeakerState.READY) {
                    state = SpeakerState.SPEAKING
                }
                if (state == SpeakerState.SPEAKING) {
                    ttsStateListener.onUtteranceStarted(articleState!!)
                    var text = articleState!!.current()!!
                    ttsClient.speechViewSpeak(cleanUtterance(text), text.md5()) {
                        if (it == utteranceId(text)) {
                            ttsStateListener.onUtteranceEnded(articleState!!)
                            if (articleState!!.hasNext()) {
                                articleState = articleState!!.next()!! // Advance again after completion
                            } else {
                                state = SpeakerState.NOT_READY
                                ttsStateListener.onSpeechStopped()
                                runBlocking {
                                    ttsStateListener.onArticleFinished(articleState!!)
                                }
                            }
                            msg.speakerState.complete(state)
                        }
                    }
                } else {
                    msg.speakerState.complete(state)
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
object StopSeakingAndMarkNotReady: TtsMsg() // Mark as ready to speak
class GetSpeakerState(val response: CompletableDeferred<SpeakerState>): TtsMsg()
class SpeakOne(val speakerState: CompletableDeferred<SpeakerState>) : TtsMsg()
object StopSpeaking : TtsMsg()

@Deprecated("Soon to be removed")
class UpdateArticleStateDeprecated(val articleState: ArticleState, val position: String): TtsMsg()
class TTSUpdateArticleState(val articleState: ArticleState): TtsMsg()

class TTSForwardOne(val newArticleState: CompletableDeferred<ArticleState>): TtsMsg()
class TTSBackOne(val newArticleState: CompletableDeferred<ArticleState>): TtsMsg()
class TTSGetArticleState(val articleState: CompletableDeferred<ArticleState>): TtsMsg()