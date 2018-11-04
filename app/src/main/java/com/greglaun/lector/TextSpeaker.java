package com.greglaun.lector;

import android.speech.tts.UtteranceProgressListener;

import com.greglaun.lector.data.model.speakable.TTSContract;
import com.greglaun.lector.data.model.speakable.TmpTxtBuffer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TextSpeaker extends UtteranceProgressListener {

    public interface Callback { void call(); }

    private TextProvider provider;
    private volatile boolean speaking;
    private final TTSContract.AudioView tts;
    TmpTxtBuffer buffer = new TmpTxtBuffer();
    Executor speechExecutor = Executors.newSingleThreadExecutor();
    private Callback endOfArticleCallback;

    public TextSpeaker (TTSContract.AudioView tts) {
        this.tts = tts;
    }

    private void queueForSpeaking(String text) {
        tts.speak(text);
    }

    public void startSpeaking(final TextProvider provider) {
        this.provider = provider;
        buffer.addFromProvider(provider);
        speaking = true;
        speechExecutor.execute(new MainSpeechLoop());
    }

    public void stopSpeaking() {
        if (speaking) {
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

    private class MainSpeechLoop implements Runnable {
        @Override
        public void run() {
            playOneIfSpeaking();
        }
    }

    public void playOneIfSpeaking() {
        if (speaking) {
            String textToSpeak = buffer.getCurrent();
            if (textToSpeak.equals(TextProvider.END_OF_STREAM)) {
                if (endOfArticleCallback != null) {
                    endOfArticleCallback.call();
                }
                return;
            }
            queueForSpeaking(textToSpeak);
        }
    }

    public void flush() {
        buffer.clear();
    }

    public String getCurrentUtterance() {
        return buffer.getCurrent();
    }

    public void setEndOfArticleCallback(Callback endOfArticleCallback) {
        this.endOfArticleCallback = endOfArticleCallback;
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {
        buffer.advance();
        playOneIfSpeaking();
    }

    @Override
    public void onError(String utteranceId) {

    }

}
