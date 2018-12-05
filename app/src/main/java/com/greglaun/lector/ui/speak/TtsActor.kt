package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.POSITION_BEGINNING
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
            is UpdateArticleState -> {
                state = SpeakerState.NOT_READY
                articleState = msg.articleState
                if (articleState != null && articleState.iterator != null &&
                        articleState.iterator.hasNext() && msg.position != POSITION_BEGINNING) {
                    state = SpeakerState.SCRUBBING
                    articleState = fastForward(articleState, msg.position)
                }
                state = SpeakerState.READY
            }
            is MarkReady -> state = SpeakerState.READY
            is MarkNotReady -> {
                ttsClient.stopSpeechViewImmediately()
                state = SpeakerState.NOT_READY
            }
            is GetSpeakerState -> msg.response.complete(state)
            is StartSpeaking -> {
                if (state == SpeakerState.READY) {
                    state = SpeakerState.SPEAKING
                }
            }
            is StopSpeaking -> {
                ttsClient.stopSpeechViewImmediately()
                state = SpeakerState.READY
            }
            is ForwardOne -> {
                if (articleState != null && articleState.iterator != null &&
                        articleState.iterator.hasNext()) {
                    val initialState = state
                    state = SpeakerState.SCRUBBING
                    ttsClient.stopSpeechViewImmediately()
                    articleState.iterator.next()
                    state = initialState
                }
            }
            is BackOne -> {
                if (articleState != null && articleState.iterator != null &&
                        articleState.iterator.hasPrevious()) {
                    val initalState = state
                    state = SpeakerState.SCRUBBING
                    ttsClient.stopSpeechViewImmediately()
                    articleState.iterator.previous()
                    state = initalState
                }
            }
            is AdvanceToPosition -> {
            }
            is GetPosition -> {
                msg.position.complete(position)
            }
            is SpeakOne -> {
                if (!articleState!!.iterator.hasNext()) {
                    state = SpeakerState.NOT_READY
                    // ttsClient.onArticleOver()
                }
                if (state == SpeakerState.SPEAKING && articleState!!.iterator != null) {
                    val text = articleState!!.iterator.next()
                    if (articleState.iterator.hasPrevious()) {
                        articleState.iterator.previous() // Return to where we were in case resume
                    }
                    ttsClient.speechViewSpeak(text) {
                        if (it == utteranceId(text)) {
                            position = it
                            if (articleState?.iterator?.hasNext()) {
                                articleState.iterator.next() // Advance again after completion
                            }
                            if (articleState?.iterator?.hasNext()) {
                                msg.speakerState.complete(state) // There is still more to speak
                            } else { // Article is over
                                state = SpeakerState.NOT_READY
                                msg.speakerState.complete(state)
                                // ttsClient.onArticleOver()
                            }
                        }
                    }
                } else {
                    msg.speakerState.complete(state)
                }
            }
        }
    }
})

fun fastForward(inState: ArticleState, position: String): ArticleState {
    var articleState = inState
    val initialIndex = articleState.iterator.nextIndex()
    var text = articleState.iterator.next()
    if (position == utteranceId(text)) {
        return articleState
    }
    while (articleState!!.iterator!!.hasNext() && position != utteranceId(text)) {
        text = articleState.iterator.next()
    }
    if (position != utteranceId(text)) {
        articleState = ArticleState(articleState.title,
                articleState.paragraphs,
                articleState.paragraphs.listIterator(initialIndex))
    }
    return articleState
}

enum class SpeakerState {
    NOT_READY,
    READY,
    SCRUBBING,
    SPEAKING
}

// Message types for ttsActor
sealed class TtsMsg
object MarkReady: TtsMsg() // Mark as ready to speak
object MarkNotReady: TtsMsg() // Mark as ready to speak
class GetSpeakerState(val response: CompletableDeferred<SpeakerState>): TtsMsg()
object StartSpeaking: TtsMsg()
class SpeakOne(val speakerState: CompletableDeferred<SpeakerState>) : TtsMsg()
object StopSpeaking : TtsMsg()
class UpdateArticleState(val articleState: ArticleState, val position: String): TtsMsg()
class AdvanceToPosition(val position: String): TtsMsg()
object ForwardOne: TtsMsg()
object BackOne: TtsMsg()
class GetPosition(val position: CompletableDeferred<String>): TtsMsg()