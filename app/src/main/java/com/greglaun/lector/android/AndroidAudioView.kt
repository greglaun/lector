package com.greglaun.lector.android

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.ui.speak.TTSContract
import java.util.*

class AndroidAudioView(val androidTts : TextToSpeech) : TTSContract.AudioView,
        UtteranceProgressListener() {
    var originalSpeed = 1.0f
    val callbacks : HashMap<String, (String) -> Unit> = HashMap()
    override fun speak(textToSpeak: String, utteranceId: String, callback : (String)-> Unit) {
        if (textToSpeak == "") {
            if (callback != null) {
                callback(utteranceId(""))
            }
            return
        }
        callbacks.put(utteranceId, callback)
        androidTts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null,
                utteranceId)
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

    override fun toggleHandsomeBritish() {
        if (androidTts.voice.locale == Locale.UK) {
            androidTts.voice = androidTts.defaultVoice
            androidTts.setSpeechRate(2.7f)
            return
        }
        val britishVoices = mutableListOf<Voice>()
        var britishVoice: Voice? = null
        val voices = androidTts.voices
        for (voice in voices) {
            if (voice.name == "en-GB-language") {
                britishVoice = voice
                continue
            }
            if (voice.locale == Locale.UK && !voice.isNetworkConnectionRequired &&
                    voice.quality >= Voice.QUALITY_VERY_HIGH) {
                britishVoices.add(voice)
            }
        }
        if (britishVoice != null) {
            androidTts.voice = britishVoice
            androidTts.setSpeechRate(1.0f)
        } else {
            if (britishVoices.size != 0) {
                androidTts.voice = britishVoices.get(0) // Just set any old British voiceo
                androidTts.setSpeechRate(1.0f)
            }
        }
    }
}