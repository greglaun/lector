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
    var onArticleOver: (() -> Unit) = {}

    // todo(testing): How do we test coroutines?
    fun ttsActor() = CoroutineScope(actorContext).actor<TtsMsg>(Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
        var articleState: ArticleState? = null
        var readyToSpeak = false
        var isSpeaking = false
        for (msg in channel) { // iterate over incoming messages
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
                    stopSpeechViewImmediately()
                    isSpeaking = false
                }
                is SpeakOne -> {
                    if (readyToSpeak && articleState!!.iterator != null) {
                        if (!articleState.iterator.hasNext()) {
                            onArticleOver()
                        }
                        val text = articleState!!.iterator.next()
                        if (articleState.iterator.hasPrevious()) {
                            articleState.iterator.previous() // Return to where we were in case resume
                        }
                        speechViewSpeek(text) {
                            if (it == utteranceId(text)) {
                                if (articleState?.iterator?.hasNext()) {
                                    articleState.iterator.next() // Advance again after completion
                                } else { // Article is over
                                    isSpeaking = false
                                    readyToSpeak = false
                                }
                                msg.speakingState.complete(isSpeaking)
                                onArticleOver()
                            }
                        }
                    }
                }
            }
        }
    })

    private fun speechViewSpeek(text: String, callback: (String) -> Unit) {
        synchronized(tts) {
            tts.speak(text) {
                callback(it)
            }
        }
    }

    private fun stopSpeechViewImmediately() {
        tts.stopImmediately()
    }

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
            actorLoop?.send(StartSpeaking)
            var stillSpeaking = true
            while(stillSpeaking) {
                val speakingState = CompletableDeferred<Boolean>()
                actorLoop?.send(SpeakOne(speakingState))
                stillSpeaking = speakingState.await()
            }
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
class GetReadyState(val response: CompletableDeferred<Boolean>): TtsMsg()
object StartSpeaking: TtsMsg()
class SpeakOne(val speakingState: CompletableDeferred<Boolean>) : TtsMsg()
object StopSpeaking : TtsMsg()
class UpdateArticleState(val articleState: ArticleState): TtsMsg()