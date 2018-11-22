package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.actor

private val actorContext = newSingleThreadContext("ActorContext")

fun ttsActor(ttsClient: TtsActorClient) = CoroutineScope(actorContext).actor<TtsMsg>(
        Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
    var articleState: ArticleState? = null
    var readyToSpeak = false
    var isSpeaking = false
    for (msg in channel) {
        when (msg) {
            is UpdateArticleState -> {
                readyToSpeak = false
                articleState = msg.articleState
            }
            is MarkReady -> readyToSpeak = true
            is GetReadyState -> msg.response.complete(readyToSpeak)
            is StartSpeaking -> {
                isSpeaking = true
            }
            is StopSpeaking -> {
                ttsClient.stopSpeechViewImmediately()
                isSpeaking = false
            }
            is SpeakOne -> {
                if (readyToSpeak && articleState!!.iterator != null) {
                    if (!articleState.iterator.hasNext()) {
                        ttsClient.onArticleOver()
                    }
                    val text = articleState!!.iterator.next()
                    if (articleState.iterator.hasPrevious()) {
                        articleState.iterator.previous() // Return to where we were in case resume
                    }
                    ttsClient.speechViewSpeak(text) {
                        if (it == utteranceId(text)) {
                            if (articleState?.iterator?.hasNext()) {
                                articleState.iterator.next() // Advance again after completion
                            } else { // Article is over
                                isSpeaking = false
                                readyToSpeak = false
                            }
                            msg.speakingState.complete(isSpeaking)
                            ttsClient.onArticleOver()
                        }
                    }
                }
            }
        }
    }
})


// Message types for ttsActor
sealed class TtsMsg
object MarkReady : TtsMsg() // Mark as ready to speak
class GetReadyState(val response: CompletableDeferred<Boolean>): TtsMsg()
object StartSpeaking: TtsMsg()
class SpeakOne(val speakingState: CompletableDeferred<Boolean>) : TtsMsg()
object StopSpeaking : TtsMsg()
class UpdateArticleState(val articleState: ArticleState): TtsMsg()
