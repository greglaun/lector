package com.greglaun.lector

import android.net.Uri
import android.util.Log
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.data.model.speakable.TmpTxtBuffer
import com.greglaun.lector.ui.main.MainContract
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.Semaphore

class TtsPresenter(private val tts: TTSContract.AudioView,
                   private val mainPresenter : MainContract.Presenter) : TTSContract.Presenter {
    private val TAG = TtsPresenter::class.java.simpleName

    val readyLock  = Semaphore(1)
    var articleStarted = false
    var provider: TextProvider? = null
    private var buffer = TmpTxtBuffer()
    private var mainLoop : Job? = null

    override fun onUrlChanged(urlString: String) {
        mainPresenter.stopSpeakingAndEnablePlayButton()
        GlobalScope.launch {
            try {
                provider = JSoupTextProvider.createFromUri(Uri.parse(urlString)).await()
                if (provider != null) {
                    // todo(mvp): Remove dependency on JoupTextProvider
                    buffer.clear()
                    buffer.addFromProvider(provider as JSoupTextProvider)
                }
                articleStarted = true
            } catch (e: Exception) {
                Log.d(TAG, "JSoupTextProvider failed", e)
            }
        }
    }


    override fun startSpeaking() {
        // todo(concurrency): Is a semaphore really the right solution here?
        mainLoop = GlobalScope.launch {
           if (buffer?.isExhausted() && articleStarted) {
                mainPresenter.onArticleOver()
                return@launch
            }
            readyLock.release()
            while (true) {
                if (buffer.isExhausted()) {
                    mainPresenter.onArticleOver()
                }
                readyLock.acquire()
                val text = buffer.getCurrent()
                tts.speak(text) {
                    if (it == utteranceId(text)) {
                        buffer.advance()
                        readyLock.release()
                    }

                }
            }
        }
    }

    override fun stopSpeaking() {
        mainLoop?.cancel()
        tts.stopImmediately()
    }

    //    private fun isWikiUrl(url: String): Boolean {
    //        return (url.startsWith("https://" + WIKI_LANGUAGE + ".wikipedia.org/wiki")
    //                || url.startsWith(
    //                "https://" + WIKI_LANGUAGE + ".m.wikipedia.org/wiki")
    //                && !url.contains("File:")
    //        )
    //    }


}
