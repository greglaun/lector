package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.Semaphore

class TtsPresenter(private val tts: TTSContract.AudioView,
                   private val provider: TextProvider) : TTSContract.Presenter {
    //todo(concurrency): Can we remove the semaphore, perhaps with proper use of coroutines?
    val readyLock  = Semaphore(1)
    private var mainLoop : Job? = null

    override fun onUrlChanged(urlString: String) {
        stopSpeaking()
        GlobalScope.launch {
            readyLock.acquire()
            provider.onUrlChanged(urlString).await()
            readyLock.release()
        }
    }

    override fun startSpeaking(onArticleOver: () -> Unit) {
        mainLoop = GlobalScope.launch {
            while (true) {
                if (provider.articleIsOver()) {
                    onArticleOver()
                }
                readyLock.acquire()
                val text = provider.getCurrent()
                if (text == null) {
                    stopSpeaking()
                }
                tts.speak(text!!) {
                    if (it == utteranceId(text)) {
                        provider.advance()
                        readyLock.release()
                    }
                }
            }
        }
    }

    override fun stopSpeaking() {
        mainLoop?.cancel()
        readyLock.release()
        tts.stopImmediately()
    }
}
