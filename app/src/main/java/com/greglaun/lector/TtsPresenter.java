package com.greglaun.lector;

import android.speech.tts.UtteranceProgressListener;

import com.greglaun.lector.ui.speak.TTSContract;
import com.greglaun.lector.data.model.speakable.TmpTxtBuffer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TtsPresenter extends UtteranceProgressListener
        implements TTSContract.Presenter {
    public interface Callback { void call(); }

    private TextProvider provider;
    private volatile boolean speaking;
    private final TTSContract.AudioView tts;
    TmpTxtBuffer buffer = new TmpTxtBuffer();
    Executor speechExecutor = Executors.newSingleThreadExecutor();
    private Callback endOfArticleCallback;

    public TtsPresenter(TTSContract.AudioView tts) {
        this.tts = tts;
    }

    @Override
    public void onUrlChanged(@NotNull String urlString) {
        // todo: implement
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

    @Override
    public void startSpeaking() {
        // todo: Implement method
    }

    @Override
    public void stopSpeaking() {
        // todo: Implement method
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

//    private fun isWikiUrl(url: String): Boolean {
//        return (url.startsWith("https://" + WIKI_LANGUAGE + ".wikipedia.org/wiki")
//                || url.startsWith(
//                "https://" + WIKI_LANGUAGE + ".m.wikipedia.org/wiki")
//                && !url.contains("File:")
//        )
//    }


}
