package com.greglaun.lector.android

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.ui.speak.TTSContract

class AndroidAudioView(val androidTts : TextToSpeech) : TTSContract.AudioView,
        UtteranceProgressListener() {
    val callbacks : HashMap<String, (String) -> Unit> = HashMap()
    override fun speak(textToSpeak: String, callback : (String)-> Unit) {
        if (textToSpeak == "") {
            if (callback != null) {
                callback(utteranceId(""))
            }
            return
        }
        callbacks.put(utteranceId(textToSpeak), callback)
        androidTts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null,
                utteranceId(textToSpeak))
    }

    override fun stopImmediately() {
        androidTts.playSilentUtterance(10, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onError(utteranceId: String?) {
        // Do nothing
    }

    override fun onStart(utteranceId: String?) {
        // Do nothing
    }

    override fun onDone(utteranceId: String?) {
        if (utteranceId != null) {
            val callback = callbacks.get(utteranceId)
            if (callback != null) {
                callback(utteranceId)
            }
        }
    }
}