package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor

class TtsPresenter(private val tts: TTSContract.AudioView)
    : TTSContract.Presenter {
    private var actorLoop: SendChannel<TtsMsg>? = null
    val actorContext = newSingleThreadContext("ActorContext")
    val workerContext = newFixedThreadPoolContext(2, "WorkerContext")
    var onArticleOver: (() -> Unit)? = {}

    fun ttsActor() = CoroutineScope(actorContext).actor<TtsMsg>(Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
        var articleState: ArticleState? = null
        var readyToSpeak = false
        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is UpdateArticleState -> {
                    readyToSpeak = false
                    articleState = msg.articleState
                }
                is MarkReady -> readyToSpeak = true
                is GetReadyState -> msg.response.complete(readyToSpeak)
                is StopSpeaking -> {
                    tts.stopImmediately()
                }
                is Speak -> if (readyToSpeak && articleState!!.iterator != null) {
                    if (!articleState.iterator.hasNext()) {
                        onArticleOver
                    }
                    val text = articleState!!.iterator.next()
                    if (articleState.iterator.hasPrevious()) {
                        articleState.iterator.previous() // Return to where we were in case resume
                    }
                    synchronized(tts) {
                        tts.speak(text) {
                            if (it == utteranceId(text)) {
                                if (articleState?.iterator?.hasNext()) {
                                    articleState.iterator.next() // Advance again after completion
                                }
                            }
                        }
                    }
                }
            }
        }
    })

    override fun onStart() {
        actorLoop = ttsActor()
    }

    override fun onStop() {
        actorLoop?.close()
    }

    override fun registerArticleOverCallback(onArticleOver: () -> Unit) {
        this.onArticleOver = onArticleOver
    }

    override fun onUrlChanged(urlString: String) {
        CoroutineScope(workerContext).launch {
            actorLoop?.send(StopSpeaking)
            val articleState = jsoupStateFromUrl(urlString)
            actorLoop?.send(UpdateArticleState(articleState))
            actorLoop?.send(MarkReady)
        }
    }

    override fun startSpeaking() {
        CoroutineScope(workerContext).launch {
            var readyStatus = false
            while(!readyStatus) {
                val readyDeferred = CompletableDeferred<Boolean>()
                actorLoop?.send(GetReadyState(readyDeferred))
                if (readyDeferred.await()) {
                    readyStatus = true
                } else {
                    delay(250)
                }
            }
            actorLoop?.send(Speak)
        }
    }

    override fun stopSpeaking() {
        CoroutineScope(workerContext).launch {
            actorLoop?.send(StopSpeaking)
        }
    }
}

// Message types for ttsActor
sealed class TtsMsg
object MarkReady : TtsMsg() // Mark as ready to speak
class GetReadyState(val response: CompletableDeferred<Boolean>) : TtsMsg()
object Speak : TtsMsg()
object StopSpeaking : TtsMsg()
class UpdateArticleState(val articleState: ArticleState): TtsMsg()
