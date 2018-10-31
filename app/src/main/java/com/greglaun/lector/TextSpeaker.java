package com.greglaun.lector;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.greglaun.lector.data.model.speakable.TmpTxtBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.NotImplementedError;

public class TextSpeaker {

    interface Callback { void call(); }

    private TextProvider provider;
    private volatile boolean speaking;
    private final CountDownLatch speechReady = new CountDownLatch(1);
    private final TextToSpeech tts;
    TmpTxtBuffer buffer = new TmpTxtBuffer();
    Executor speechExecutor = Executors.newSingleThreadExecutor();
    private Callback endOfArticleCallback;

    public TextSpeaker (Context context) {
        this(context, null);
    }

    public TextSpeaker(Context context, Callback endOfArticleCallback) {
        tts = new TextToSpeech(context, status -> speechReady.countDown());
        this.endOfArticleCallback = endOfArticleCallback;
    }

    private void queueForSpeaking(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public void startSpeaking(final TextProvider provider) {
        this.provider = provider;
        buffer.addFromProvider(provider);
        speaking = true;
        speechExecutor.execute(new MainSpeechLoop());
    }

    public void stopSpeaking() {
        if (speaking) {
            tts.playSilentUtterance(0, TextToSpeech.QUEUE_FLUSH, null);
            speaking = false;
            tts.stop();
        }
    }

    public TextProvider getProvider() {
        return provider;
    }

    public void setProvider(TextProvider provider) {
        this.provider = provider;
    }

    public String nextSpeechUnit() {
        return buffer.getCurrentAndAdvance();
    }

    private class MainSpeechLoop implements Runnable {
        @Override
        public void run() {
            while (speaking) {
                String textToSpeak = nextSpeechUnit();
                if (textToSpeak.equals(TextProvider.END_OF_STREAM)) {
                    if (endOfArticleCallback != null) {
                        endOfArticleCallback.call();
                    }
                    return;
                }
                queueForSpeaking(textToSpeak);
            }
        }
    }

    public String getCurrentUtterance() {
        throw new NotImplementedError();
    }

    public void setEndOfArticleCallback(Callback endOfArticleCallback) {
        this.endOfArticleCallback = endOfArticleCallback;
    }
}
