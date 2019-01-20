package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.POSITION_BEGINNING
import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.utteranceId
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.actor

private val actorContext = newSingleThreadContext("ActorContext")

fun ttsActor(ttsClient: TtsActorClient, ttsStateListener: TtsStateListener) =
        CoroutineScope(actorContext).actor<TtsMsg>(
                Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
    var articleState: ArticleState? = null
    var state = SpeakerState.NOT_READY
    var position: String = POSITION_BEGINNING
    for (msg in channel) {
        when (msg) {
//            is UpdateArticleStateDeprecated -> {
//                state = SpeakerState.NOT_READY
//                articleState = msg.articleState
//                if (articleState != null && articleState.current_index != null &&
//                        articleState.hasNext() && msg.position != POSITION_BEGINNING) {
//                    state = SpeakerState.SCRUBBING
//                    articleState = fastForward(articleState, msg.position)
//                }
//                state = SpeakerState.READY
//            }
            is UpdateArticleState -> {
                state = SpeakerState.NOT_READY
                articleState = msg.articleState
                state = SpeakerState.READY
            }
            is MarkReady -> state = SpeakerState.READY
            is StopSeakingAndMarkNotReady -> {
                ttsClient.stopSpeechViewImmediately()
                ttsStateListener.onSpeechStopped()
            }
            is GetSpeakerState -> msg.response.complete(state)
            is StartSpeaking -> {
                if (state == SpeakerState.READY) {
                    state = SpeakerState.SPEAKING
                }
            }
            is StopSpeaking -> {
                val previousState = state
                ttsClient.stopSpeechViewImmediately()
                if (previousState == SpeakerState.SPEAKING) {
                    state = SpeakerState.READY
                } else {
                    state = previousState
                }
            }
            is ForwardOne -> {
                if (articleState != null && articleState.current_index != null &&
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
            is BackOne -> {
                if (articleState != null && articleState.current_index != null &&
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
            is GetPosition -> {
                msg.position.complete(position)
            }
            is SpeakOne -> {
                if (state == SpeakerState.SPEAKING) {
                    ttsStateListener.onUtteranceStarted(articleState!!)
                    var text = articleState!!.current()!!
                    ttsClient.speechViewSpeak(cleanUtterance(text), text.md5()) {
                        if (it == utteranceId(text)) {
                            ttsStateListener.onUtteranceEnded(articleState!!)
                            position = it
                            if (articleState!!.hasNext()) {
                                articleState = articleState!!.next()!! // Advance again after completion
                            } else {
                                state = SpeakerState.NOT_READY
                                ttsStateListener.onSpeechStopped()
                                ttsStateListener.onArticleFinished(articleState!!)
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
object MarkReady: TtsMsg() // Mark as ready to speak
object StopSeakingAndMarkNotReady: TtsMsg() // Mark as ready to speak
class GetSpeakerState(val response: CompletableDeferred<SpeakerState>): TtsMsg()
object StartSpeaking: TtsMsg()
class SpeakOne(val speakerState: CompletableDeferred<SpeakerState>) : TtsMsg()
object StopSpeaking : TtsMsg()

@Deprecated("Soon to be removed")
class UpdateArticleStateDeprecated(val articleState: ArticleState, val position: String): TtsMsg()
class UpdateArticleState(val articleState: ArticleState): TtsMsg()

class ForwardOne(val newArticleState: CompletableDeferred<ArticleState>): TtsMsg()
class BackOne(val newArticleState: CompletableDeferred<ArticleState>): TtsMsg()
class GetPosition(val position: CompletableDeferred<String>): TtsMsg()